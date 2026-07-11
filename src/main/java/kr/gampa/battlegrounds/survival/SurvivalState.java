package kr.gampa.battlegrounds.survival;
public final class SurvivalState {
  private double health; private boolean downed; private boolean dead;
  private SurvivalState(double health) { this.health = health; }
  public static SurvivalState alive(double health) { if (health <= 0) throw new IllegalArgumentException("health"); return new SurvivalState(health); }
  public DamageOutcome applyDamage(double damage, boolean hasLivingTeammate, boolean forceKill) {
    if (dead || damage <= 0) return dead ? DamageOutcome.KILLED : DamageOutcome.SURVIVED;
    if (downed || forceKill || (!hasLivingTeammate && damage >= health)) { health = 0; downed = false; dead = true; return DamageOutcome.KILLED; }
    if (damage >= health) { health = 1; downed = true; return DamageOutcome.KNOCKED; }
    health -= damage; return DamageOutcome.SURVIVED;
  }
  public double heal(double amount, double cap) { if (dead || downed || amount <= 0) return health; health = Math.min(cap, health + amount); return health; }
  public boolean revive(double revivedHealth) { if (!downed || dead || revivedHealth <= 0) return false; downed = false; health = revivedHealth; return true; }
  public double health() { return health; } public boolean downed() { return downed; } public boolean dead() { return dead; }
}
