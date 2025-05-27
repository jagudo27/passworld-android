package mobile.passworld.data;

import java.time.LocalDateTime;

public class PasswordDTO {
    private int id;
    private String idFb;
    private String description;
    private String username;
    private String url;
    private String password;
    private boolean isWeak;
    private boolean isDuplicate;
    private boolean isCompromised;
    private boolean isUrlUnsafe;
    private String lastModified;
    private boolean isSynced;



    // Constructor, getters y setters
    public PasswordDTO(String description, String username, String url, String password, String lastModified) {
        this.description = description;
        this.username = username;
        this.url = url;
        this.password = password;
        this.lastModified = lastModified;
        this.isSynced = false;
    }

    public PasswordDTO(String description, String username, String url, String password) {
        this.description = description;
        this.username = username;
        this.url = url;
        this.password = password;
    }
    public PasswordDTO() {
    }

    // Agregar getter y setter para createdAt

    public void setLastModified(String createdAt) {
        this.lastModified = createdAt;
    }

    public String getIdFb() {
        return idFb;
    }

    public void setIdFb(String id) {
        this.idFb = id;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isWeak() {
        return isWeak;
    }

    public void setWeak(boolean weak) {
        isWeak = weak;
    }

    public boolean isDuplicate() {
        return isDuplicate;
    }

    public void setDuplicate(boolean duplicate) {
        isDuplicate = duplicate;
    }

    public boolean isCompromised() {
        return isCompromised;
    }

    public void setCompromised(boolean compromised) {
        isCompromised = compromised;
    }

    public boolean isUrlUnsafe() {
        return isUrlUnsafe;
    }

    public void setUrlUnsafe(boolean urlUnsafe) {
        isUrlUnsafe = urlUnsafe;
    }

    public boolean isSynced() {
        return isSynced;
    }
    public void setSynced(boolean synced){
        isSynced = synced;
    }
    public LocalDateTime getlastModified() {
        if (lastModified == null || lastModified.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(lastModified);
    }
    @Override
    //toString solo password e id
    public String toString() {
        return "PasswordDTO{" +
                "id=" + id +
                ", idFb='" + idFb + '\'' +
                ", description='" + description + '\'' +
                ", username='" + username + '\'' +
                ", url='" + url + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

}