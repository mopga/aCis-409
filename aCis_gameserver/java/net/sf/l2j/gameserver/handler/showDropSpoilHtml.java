package net.sf.l2j.gameserver.handler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


import net.sf.l2j.gameserver.enums.DropType;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.item.DropCategory;
import net.sf.l2j.gameserver.model.item.DropData;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;


public class showDropSpoilHtml {

  private static final ConcurrentHashMap<Integer, Cache> CACHE = new ConcurrentHashMap<>();
  private static final long TTL_NANOS = TimeUnit.MINUTES.toNanos(5);

  private showDropSpoilHtml() {}

  public static void buildAndSend(Player player, Attackable mob) {
      final boolean isRaid = mob.isRaidBoss(); // или mob.isRaidBoss() в твоей ветке
      final int cacheKey = mob.getNpcId() ^ (isRaid ? 0x40000000 : 0); // разный key для рейдов
      final String html = getOrBuild(mob.getTemplate(), cacheKey, isRaid);
      NpcHtmlMessage msg = new NpcHtmlMessage(0);
      msg.setHtml(html);
      player.sendPacket(msg);
  }

  // ----- internals -----

  private static String getOrBuild(NpcTemplate tpl, int cacheKey, boolean isRaid) {
      final long now = System.nanoTime();
      Cache c = CACHE.get(cacheKey);
      if (c != null && now - c.ts < TTL_NANOS) return c.html;
      String html = buildHtml(tpl, isRaid);
      CACHE.put(cacheKey, new Cache(html, now));
      return html;
  }

  private static String buildHtml(NpcTemplate tpl, boolean isRaid) {
      StringBuilder sb = new StringBuilder(4096);
      sb.append("<html><title>Loot</title><body>");
      sb.append("<center><img src=\"icon.etc_crystal_gold_i00\" width=32 height=32></center>");
      sb.append("<center><font color=LEVEL>")
        .append(esc(tpl.getName()))
        .append(" (ID: ").append(tpl.getNpcId()).append(")</font></center><br>");

      appendDropBlock(sb, "Дроп (drop)", tpl, DropType.DROP, isRaid);
      sb.append("<br>");
      appendDropBlock(sb, "Спойл (spoil)", tpl, DropType.SPOIL, isRaid);
      sb.append("<br>");
      appendDropBlock(sb, "Валюты (currency)", tpl, DropType.CURRENCY, isRaid);

      sb.append("</body></html>");
      return sb.toString();
  }


  private static String esc(String s) {
    return s == null ? "" : s.replace("<","&lt;").replace(">","&gt;");
  }

  private static final class Cache {
    final String html; final long ts;
    Cache(String h, long t) { html = h; ts = t; }
  }


  private static void appendDropBlock(StringBuilder sb, String title, NpcTemplate tpl, DropType type, boolean isRaid) {
      sb.append("<font color=LEVEL>").append(title).append("</font><br>");

      var cats = tpl.getDropData();
      if (cats == null || cats.isEmpty()) {
          sb.append("<font color=777777>&nbsp;&nbsp;Нет записей</font><br>");
          return;
      }

      final double rate = type.getDropRate(isRaid); // берём из твоего enum

      var items = new java.util.ArrayList<View>(64);
      for (DropCategory cat : cats) {
          if (cat == null || cat.getDropType() != type || cat.isEmpty()) continue;

          final double catPct = normalizeToPercent(cat.getChance()); // 0..100

          for (DropData d : cat) {
              var it = net.sf.l2j.gameserver.data.xml.ItemData.getInstance().getTemplate(d.itemId());
              if (it == null) continue;

              int min = d.minDrop();
              int max = d.maxDrop();

              // базовый шанс предмета в %
              double itemPct = normalizeToPercent(d.chance());

              if (type == DropType.CURRENCY) {
                  // валюте множим количество, шанс не трогаем
                  min = safeMulRound(min, rate);
                  max = safeMulRound(max, rate);
                  double effPct = itemPct * (catPct / 100.0);
                  int guaranteed =  (int) Math.floor(effPct / 100.0);
                  double rem = effPct - guaranteed * 100.0;
                  items.add(new View(it.getName(), min, max, guaranteed, rem));
              } else {
                  // предметы/спойл: множим шанс на rate
                  double effPct = itemPct * (catPct / 100.0) * rate;
                  int guaranteed =  (int) Math.floor(effPct / 100.0);
                  double rem = effPct - guaranteed * 100.0;
                  items.add(new View(it.getName(), min, max, guaranteed, rem));
              }
          }
      }

      if (items.isEmpty()) {
          sb.append("<font color=777777>&nbsp;&nbsp;Нет записей</font><br>");
          return;
      }

      // сортируем по убыванию: сначала гарант >, затем остаток
      items.sort((a, b) -> {
          int cmpG = Integer.compare(b.guaranteed, a.guaranteed);
          if (cmpG != 0) return cmpG;
          return Double.compare(b.remPct, a.remPct);
      });

      // вывод в 1 строку: "Название — шанс: 100%xg + X% (кол-во: gxmin-gxmax [+min-max])"
      for (View v : items) {
          sb.append("&nbsp;&nbsp;").append(esc(v.name)).append(" — шанс: ");
          if (v.guaranteed > 0) {
              sb.append("100%x").append(v.guaranteed);
              if (v.remPct > 0.0) sb.append(" + ").append(formatChanceRated(v.remPct));
          } else {
              sb.append(formatChanceRated(v.remPct));
          }

          // Кол-во: гарантированная часть даёт gx[min..max], остаток — ещё +[min..max] с шансом rem
          sb.append(" (кол-во: ");
          if (v.guaranteed > 0) {
              int gMin = safeMulRound(v.min, v.guaranteed);
              int gMax = safeMulRound(v.max, v.guaranteed);
              if (gMin == gMax) sb.append(gMin);
              else sb.append(gMin).append("-").append(gMax);
              if (v.remPct > 0.0) sb.append(" + ").append(v.min == v.max ? v.min : (v.min + "-" + v.max));
          } else {
              if (v.min == v.max) sb.append(v.min);
              else sb.append(v.min).append("-").append(v.max);
          }
          sb.append(")");

          sb.append("<br>");
      }
  }



  // «сырой» шанс из XML в проценты 0..100
  private static double normalizeToPercent(double raw) {
      if (raw <= 0) return 0.0;
      if (raw > 1000.0) return raw / 10000.0; // 1_000_000 = 100%
      if (raw > 1.0)   return raw;            // уже проценты (0..100)
      return raw * 100.0;                     // доля 0..1
  }

  // форматируем шанс с учётом гарантированных кусков >100%
  private static String formatChanceRated(double pct) {
      if (pct <= 0.0) return "0%";
      if (pct < 1.0) {
          long denom = Math.max(1, Math.round(100.0 / pct)); // 0.12% → 1/833
          return "1/" + denom;
      }
      if (pct < 100.0) {
          return String.format(java.util.Locale.US, "%.2f%%", pct);
      }
      // 100%+ : гарантированное количество + остаток
      int guaranteed = (int) Math.floor(pct / 100.0);
      double rem = pct - guaranteed * 100.0;
      if (rem >= 1.0) {
          return "100% x" + guaranteed + " + " + String.format(java.util.Locale.US, "%.2f%%", rem);
      } else if (rem > 0.0) {
          long denom = Math.max(1, Math.round(100.0 / rem));
          return "100% x" + guaranteed + " + 1/" + denom;
      } else {
          return "100% x" + guaranteed;
      }
  }

  // безопасное умножение количества (для валюты)
  private static int safeMulRound(int base, double rate) {
      double v = base * rate;
      if (v > Integer.MAX_VALUE) return Integer.MAX_VALUE;
      return (int) Math.round(v);
  }

private static final class View {
    final String name;
    final int min, max;
    final int guaranteed;   // число гарантированных проков (каждый даёт min..max)
    final double remPct;    // шанс на ещё один прок
    View(String n, int mi, int ma, int g, double r) { name=n; min=mi; max=ma; guaranteed=g; remPct=r; }
}

}
    

