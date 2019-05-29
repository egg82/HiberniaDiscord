package co.paradaux.hdiscord.core;

public class CachedConfigValues {
    private CachedConfigValues() {}

    private boolean debug = false;
    public boolean getDebug() { return debug; }

    private String avatarURL = "https://crafatar.com/renders/head/";
    public String getAvatarURL() { return avatarURL; }

    private String avatarOptions = "size=10&overlay";
    public String getAvatarOptions() { return avatarOptions; }

    public static CachedConfigValues.Builder builder() { return new CachedConfigValues.Builder(); }

    public static class Builder {
        private final CachedConfigValues values = new CachedConfigValues();

        private Builder() {}

        public CachedConfigValues.Builder debug(boolean value) {
            values.debug = value;
            return this;
        }

        public CachedConfigValues.Builder avatarURL(String value) {
            if (value == null) {
                throw new IllegalArgumentException("value cannot be null.");
            }

            values.avatarURL = value;
            return this;
        }

        public CachedConfigValues.Builder avatarOptions(String value) {
            if (value == null) {
                throw new IllegalArgumentException("value cannot be null.");
            }

            values.avatarOptions = value;
            return this;
        }

        public CachedConfigValues build() { return values; }
    }
}
