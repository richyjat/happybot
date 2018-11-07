package io.github.jroy.happybot.commands.levels;

import io.github.jroy.happybot.commands.base.CommandBase;
import io.github.jroy.happybot.commands.base.CommandCategory;
import io.github.jroy.happybot.commands.base.CommandEvent;
import io.github.jroy.happybot.levels.Leveling;
import io.github.jroy.happybot.levels.LevelingToken;
import io.github.jroy.happybot.util.C;
import io.github.jroy.happybot.util.TextGeneration;
import net.dv8tion.jda.core.entities.Member;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class LevelCommand extends CommandBase {

  private Leveling leveling;

  public LevelCommand(Leveling leveling) {
    super("rank", "<user>", "Displays your current rank stats.", CommandCategory.FUN);
    this.aliases = new String[]{"level"};
    this.leveling = leveling;
  }

  @Override
  protected void executeCommand(CommandEvent e) {
    Member target = e.getMember();

    if (!e.getArgs().isEmpty()) {
      target = C.matchMember(target, e.getArgs());
    }

    String targetId = target.getUser().getId();

    if (!leveling.isPastUser(targetId)) {
      e.replyError("This user is not in the database!");
      return;
    }

    long totalXp = leveling.getExp(targetId);
    int level = leveling.toLevel(totalXp);
    int rankXp = leveling.getNextExp(level).intValue();
    int totalExpP = level - 1;
    if (level == 0) {
      totalExpP = 0;
    }
    long progressXp = totalXp - leveling.getTotalExp(totalExpP) - leveling.getNextExp(totalExpP).intValue();

    int rank = -1;
    for (HashMap.Entry<Integer, LevelingToken> curEntry : leveling.topCache.entrySet()) {
      if (curEntry.getValue().getMember().getUser().getId().equals(target.getUser().getId())) {
        rank = curEntry.getKey();
        break;
      }
    }

    BufferedImage card = TextGeneration.card;
    card = TextGeneration.writeTextCenter(card, C.getFullName(target.getUser()), 200F, 0);
    card = TextGeneration.writeTextCenter(card, C.prettyNum(totalXp), 75F, 215);
    card = TextGeneration.writeText(card, (rank == -1 ? "?" : String.valueOf(rank)), 150F, 415, 670);
    card = TextGeneration.writeText(card, String.valueOf(level), 150F, 325, 847);
    card = TextGeneration.writeText(card, TextGeneration.formatXpProgress(C.prettyNum(progressXp)), 100F, 2160, 840);
    card = TextGeneration.writeText(card, C.prettyNum(rankXp), 100F, 2518, 840);
    card = TextGeneration.writeImage(card, TextGeneration.calculateProgressId(progressXp, rankXp), 2315, 705);
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try {
      ImageIO.write(card, "png", os );
      e.getChannel().sendFile(new ByteArrayInputStream(os.toByteArray()), "rank.png", null).queue();
    } catch (IOException e1) {
      e1.printStackTrace();
      e.reply("lol no :heart:");
    }
  }
}
