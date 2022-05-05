package Lab2.homeautomation.shared;

public class Movie {
    private String title;

    public Movie(String title) {
        this.title = title;
    }

    public static Movie withTitle(String title) {
        return new Movie(title);
    }

    public String getTitle(){
        return this.title;
    }
}
