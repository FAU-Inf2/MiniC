package i2.act.examples.minic.bugs;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class Bugs implements Iterable<Bug> {

  private static Bugs instance;

  public static final Bugs getInstance() {
    if (instance == null) {
      instance = new Bugs();
    }

    return instance;
  }

  // ===============================================================================================

  private final EnumSet<Bug> bugs;

  private Bugs() {
    this.bugs = EnumSet.noneOf(Bug.class);
  }

  public final void enable(final Bug bug) {
    this.bugs.add(bug);
  }

  public final void enableAllOf(final Bug.Category category) {
    for (final Bug bug : Bug.values()) {
      if (bug.getCategory() == category) {
        enable(bug);
      }
    }
  }

  public final void enableAll() {
    for (final Bug bug : Bug.values()) {
      enable(bug);
    }
  }

  public final void disable(final Bug bug) {
    this.bugs.remove(bug);
  }

  public final boolean isEnabled(final Bug bug) {
    return this.bugs.contains(bug);
  }

  public final int numberOfBugs() {
    return this.bugs.size();
  }

  @Override
  public final Iterator<Bug> iterator() {
    return this.bugs.iterator();
  }

  @Override
  public final String toString() {
    return StreamSupport.stream(spliterator(), false)
        .map(Bug::getName)
        .collect(Collectors.joining(", "));
  }

}
