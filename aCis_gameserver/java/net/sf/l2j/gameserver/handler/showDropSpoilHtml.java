package net.sf.l2j.gameserver.handler;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.swing.text.View;

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
    final int npcId = mob.getNpcId();
    final String html = getOrBuild(mob.getTemplate(), npcId);
    NpcHtmlMessage msg = new NpcHtmlMessage(0);
    msg.setHtml(html);
    player.sendPacket(msg);
  }

  // ----- internals -----

  private static String getOrBuild(NpcTemplate tpl, int npcId) {
    final long now = System.nanoTime();
    Cache c = CACHE.get(npcId);
    if (c != null && now - c.ts < TTL_NANOS) return c.html;
    String html = buildHtml(tpl);
    CACHE.put(npcId, new Cache(html, now));
    return html;
  }

  private static String buildHtml(NpcTemplate tpl) {
    StringBuilder sb = new StringBuilder(4096);
    sb.append("<html><title>Loot</title><body>");
    sb.append("<center><img src=\"icon.etc_coin_gold_i00\" width=32 height=32></center>");
    sb.append("<center><font color=LEVEL>")
      .append(esc(tpl.getName()))
      .append(" (ID: ").append(tpl.getNpcId()).append(")</font></center><br>");

    appendDropBlock(sb, "Дроп (drop)", tpl, DropType.DROP);
    sb.append("<br>");
    appendDropBlock(sb, "Спойл (spoil)", tpl, DropType.SPOIL);

    sb.append("</body></html>");
    return sb.toString();
  }


  private static String esc(String s) {
    return s == null ? "" : s.replace("<","&lt;").replace(">","&gt;");
  }

  private static String formatCount(int min, int max) {
    if (min == max) return (min == 1 ? "1 шт" : (min + " шт"));
    return "от " + min + " до " + max;
  }

  private static String formatChance(double stored) {
        if (stored <= 0.0) return "0%";
        double pct = stored / DropData.PERCENT_CHANCE; // 10000 = 1%
        if (pct >= 1.0) return String.format(java.util.Locale.US, "%.2f%%", pct);
        long denom = Math.max(1, Math.round(100.0 / pct)); // 0.14% → 1/714
        return "1/" + denom;
  }

  private static final class Cache {
    final String html; final long ts;
    Cache(String h, long t) { html = h; ts = t; }
  }

private static java.util.List<DropData> getList(NpcTemplate tpl, DropType type) {
    java.util.List<DropData> out = new java.util.ArrayList<>();
    java.util.List<DropCategory> cats = tpl.getDropData(); // List<DropCategory>, а каждая категория это List<DropData>
    if (cats != null) {
        for (DropCategory cat : cats) {
            if (cat != null && cat.getDropType() == type && !cat.isEmpty()) {
                out.addAll(cat); // <- категория сама по себе List<DropData>
            }
        }
    }
    return out;
}

private static void appendDropBlock(StringBuilder sb, String title, NpcTemplate tpl, DropType type) {
    sb.append("<font color=LEVEL>").append(title).append("</font><br>");

    // собрать элементы из категорий данного типа
    java.util.List<DropCategory> cats = tpl.getDropData();
    if (cats == null || cats.isEmpty()) {
        sb.append("<font color=777777>&nbsp;&nbsp;Нет записей</font><br>");
        return;
    }

    java.util.List<View> items = new java.util.ArrayList<>(64);
    for (DropCategory cat : cats) {
        if (cat == null || cat.getDropType() != type || cat.isEmpty()) continue;

        // шанс категории (в %) – нормализуем
        double catPct = normalizeToPercent(cat.getChance()); // 0..100

        for (DropData d : cat) {
            var it = net.sf.l2j.gameserver.data.xml.ItemData.getInstance().getTemplate(d.itemId());
            if (it == null) continue;

            // шанс предмета (в %) – нормализуем
            double itemPct = normalizeToPercent(d.chance());

            // эффективный шанс на килл:
            // для DROP обычно умножаем на шанс категории; для SPOIL тоже не повредит,
            // но если у тебя cat=100, то это то же самое.
            double effPct = itemPct * (catPct / 100.0);

            items.add(new View(it.getName(), d.minDrop(), d.maxDrop(), effPct));
        }
    }

    if (items.isEmpty()) {
        sb.append("<font color=777777>&nbsp;&nbsp;Нет записей</font><br>");
        return;
    }

    // сортировка по убыванию эффективного шанса
    items.sort((a, b) -> Double.compare(b.effPct, a.effPct));

    // печать: "Название — шанс: X%"
    for (View v : items) {
        sb.append("&nbsp;&nbsp;")
          .append(esc(v.name))
          .append(" — шанс: ").append(formatChancePct(v.effPct));

        // компактный вывод количества
        if (v.min == v.max) sb.append(" (").append(v.min).append(" шт)");
        else sb.append(" (").append("от ").append(v.min).append(" до ").append(v.max).append(")");

        sb.append("<br>");
    }
}

// Приводим «как хранится в XML» к «процентам 0..100»
private static double normalizeToPercent(double raw) {
    if (raw <= 0) return 0.0;
    if (raw > 1000.0) return raw / 10000.0;  // 1_000_000 = 100%
    if (raw > 1.0)   return raw;             // уже проценты (0..100)
    return raw * 100.0;                      // доля 0..1
}

private static String formatChancePct(double pct) {
    if (pct >= 1.0) return String.format(java.util.Locale.US, "%.2f%%", pct);
    if (pct <= 0.0) return "0%";
    long denom = Math.max(1, Math.round(100.0 / pct)); // 0.12% → 1/833
    return "1/" + denom;
}

private static final class View {
    final String name;
    final int min, max;
    final double effPct;
    View(String n, int mi, int ma, double p) { name = n; min = mi; max = ma; effPct = p; }
}

}
    

