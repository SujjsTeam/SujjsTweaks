package net.sujjsteam.tweaks.ritual;


import java.util.ArrayList;
import java.util.List;

public final class RitualSky {

    public record Request(double x, double y, double z, float strength, float flash) {
    }

    private static final List<Request> REQUESTS = new ArrayList<>();

    private RitualSky() {
    }

    public static synchronized void report(double x, double y, double z, float strength, float flash) {
        REQUESTS.add(new Request(x, y, z, strength, flash));
    }

    public static synchronized List<Request> takeRequests() {
        List<Request> copy = new ArrayList<>(REQUESTS);
        REQUESTS.clear();
        return copy;
    }
}
