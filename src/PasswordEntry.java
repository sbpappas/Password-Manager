public class PasswordEntry {
    private String site;
    private String username;
    private String password;

    public PasswordEntry(String site, String username, String password) {
        this.site = site;
        this.username = username;
        this.password = password;
    }

    public String getSite() {
        return site;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "Site: " + site + ", Username: " + username;
    }
}
