import edu.princeton.cs.algs4.Picture;

import java.awt.Color;

public class SeamCarver {

    private int width;
    private int height;
    private Picture picture;
    private double[][] energies;
    private int[][] edgeToV;
    private double[][] distToV;
    private int[][] edgeToH;
    private double[][] distToH;
    private boolean hasChangedV = true;
    private boolean hasChangedH = true;

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null)
            throw new IllegalArgumentException();
        this.width = picture.width();
        this.height = picture.height();
        this.picture = new Picture(picture);
        computeAllEnergies();
    }

    // current picture
    public Picture picture() {
        return new Picture(this.picture);
    }

    // width of current picture
    public int width() {
        return width;
    }

    // height of current picture
    public int height() {
        return height;
    }

    private void computeAllEnergies() {
        this.energies = new double[height][width];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                this.energies[y][x] = energy(x, y);
            }
        }
    }

    // energy of pixel at column x and row y. Performance requirements: O(1)
    public double energy(int x, int y) {
        validatePixelRange(x, y);
        if (x == 0 || x == width - 1 || y == 0 || y == height - 1)
            return 1000;
        int deltaX = this.getSquareGradient(this.picture.get(x - 1, y), this.picture.get(x + 1, y));
        int deltaY = this.getSquareGradient(this.picture.get(x, y - 1), this.picture.get(x, y + 1));
        return Math.sqrt(deltaX + deltaY);
    }

    private void validatePixelRange(int x, int y) {
        if (x < 0 || x > width - 1 || y < 0 || y > height - 1)
            throw new IllegalArgumentException("Out of range x and y");
    }

    private int getSquareGradient(Color a, Color b) {
        int r = a.getRed() - b.getRed();
        int g = a.getGreen() - b.getGreen();
        int bl = a.getBlue() - b.getBlue();
        return r * r + g * g + bl * bl;
    }

    private void calculateVerticalPaths() {
        edgeToV = new int[height][width];
        distToV = new double[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (y == 0)
                    distToV[y][x] = 1000;
                else
                    distToV[y][x] = Double.POSITIVE_INFINITY;
            }
        }
        for (int y = 0; y < height - 1; y++) {
            for (int x = 0; x < width; x++) {
                if (x - 1 >= 0) relaxVertical(x - 1, y + 1, x);
                relaxVertical(x, y + 1, x);
                if (x + 1 < width) relaxVertical(x + 1, y + 1, x);
            }
        }
        hasChangedV = false;
    }

    private void calculateHorizontalPaths() {
        edgeToH = new int[height][width];
        distToH = new double[height][width];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (x == 0) {
                    distToH[y][x] = 1000;
                }
                else {
                    distToH[y][x] = Double.POSITIVE_INFINITY;
                }
            }
        }
        for (int x = 0; x < width - 1; x++) {
            for (int y = 0; y < height; y++) {
                if (y - 1 >= 0) relaxHorizontal(x + 1, y - 1, y);
                relaxHorizontal(x + 1, y, y);
                if (y + 1 < height) relaxHorizontal(x + 1, y + 1, y);
            }
        }
        hasChangedH = false;
    }

    private void relaxVertical(int x, int y, int upperX) {
        if (distToV[y][x] > distToV[y - 1][upperX] + energies[y][x]) {
            distToV[y][x] = distToV[y - 1][upperX] + energies[y][x];
            edgeToV[y][x] = upperX;
        }
    }

    private void relaxHorizontal(int x, int y, int leftY) {
        if (distToH[y][x] > distToH[leftY][x - 1] + energies[y][x]) {
            distToH[y][x] = distToH[leftY][x - 1] + energies[y][x];
            edgeToH[y][x] = leftY;
        }
    }

    // sequence of indices for horizontal seam (path using topological order)
    public int[] findHorizontalSeam() {
        if (hasChangedH) calculateHorizontalPaths();
        double min = Double.POSITIVE_INFINITY;
        int[] result = new int[width];
        for (int y = 0; y < height; y++) {
            if (distToH[y][width - 1] < min) {
                result[width - 1] = y;
                min = distToH[y][width - 1];
            }
        }
        for (int x = width - 2; x >= 0; x--) {
            result[x] = edgeToH[result[x + 1]][x + 1];
        }
        return result;
    }

    // sequence of indices for vertical seam (path using topological order)
    public int[] findVerticalSeam() {
        if (hasChangedV) calculateVerticalPaths();
        double min = Double.POSITIVE_INFINITY;
        int[] result = new int[height];
        for (int x = 0; x < width; x++) {
            if (distToV[height - 1][x] < min) {
                result[height - 1] = x;
                min = distToV[height - 1][x];
            }
        }
        for (int y = height - 2; y >= 0; y--) {
            result[y] = edgeToV[y + 1][result[y + 1]];
        }
        return result;
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        validateHorizontalSeam(seam);
        Picture result = new Picture(width, height - 1);
        for (int x = 0; x < result.width(); x++) {
            boolean isFound = false;
            for (int y = 0; y < result.height(); y++) {
                if (isFound ||  y == seam[x]) {
                    result.set(x, y, picture.get(x, y + 1));
                    isFound = true;
                } else {
                    result.set(x, y, picture.get(x, y));
                }
            }
        }
        height--;
        picture = result;
        this.computeAllEnergies();
        hasChangedV = true;
        hasChangedH = true;
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        validateVerticalSeam(seam);
        Picture result = new Picture(width - 1, height);
        for (int y = 0; y < result.height(); y++) {
            boolean isFound = false;
            for (int x = 0; x < result.width(); x++) {
                if (isFound ||  x == seam[y]) {
                    result.set(x, y, picture.get(x + 1, y));
                    isFound = true;
                } else {
                    result.set(x, y, picture.get(x, y));
                }
            }
        }
        width--;
        picture = result;
        this.computeAllEnergies();
        hasChangedV = true;
        hasChangedH = true;
    }

    private void validateVerticalSeam(int[] seam) {
        if (seam == null || seam.length == 0 || seam.length != height || width <= 1)
            throw new IllegalArgumentException();
        for (int i = 0; i < seam.length; i++) {
            if (seam[i] < 0 || seam[i] > width - 1)
                throw new IllegalArgumentException();
            if (i > 0 && Math.abs(seam[i] - seam[i - 1]) >= 2)
                throw new IllegalArgumentException();
        }
    }

    private void validateHorizontalSeam(int[] seam) {
        if (seam == null || seam.length == 0 || seam.length != width || height <= 1)
            throw new IllegalArgumentException();
        for (int i = 0; i < seam.length; i++) {
            if (seam[i] < 0 || seam[i] > height - 1)
                throw new IllegalArgumentException();
            if (i > 0 && Math.abs(seam[i] - seam[i - 1]) >= 2)
                throw new IllegalArgumentException();
        }
    }

    //  unit testing (optional)
    public static void main(String[] args) {
        Picture picture = new Picture("src/resources/6x5.png");
        SeamCarver sc = new SeamCarver(picture);
        sc.findVerticalSeam();
        sc.findHorizontalSeam();
        sc.removeVerticalSeam(sc.findVerticalSeam());
        sc.removeHorizontalSeam(sc.findHorizontalSeam());
    }

}