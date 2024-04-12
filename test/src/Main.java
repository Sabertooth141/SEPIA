public class Main {
    static class Coordinates {
        int x;
        int y;

        public Coordinates(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    public static void main(String[] args) {
        Coordinates a = new Coordinates(12, 14);
        Coordinates b = new Coordinates(19, 12);

        a = b;
    }
}