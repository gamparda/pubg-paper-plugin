package kr.gampa.battlegrounds.combat;
public final class Magazine {
  private final int capacity; private int rounds;
  public Magazine(int capacity) { this(capacity, capacity); }
  public Magazine(int capacity, int rounds) {
    if (capacity <= 0 || rounds < 0 || rounds > capacity) throw new IllegalArgumentException("invalid magazine");
    this.capacity = capacity; this.rounds = rounds;
  }
  public boolean fire() { if (rounds == 0) return false; rounds--; return true; }
  public int reload(int available) { int used = Math.min(Math.max(available, 0), capacity - rounds); rounds += used; return used; }
  public int rounds() { return rounds; }
  public int capacity() { return capacity; }
}
