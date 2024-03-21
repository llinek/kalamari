package cz.llinek.kalamari.dataTypes;

public interface RequestCallback {
    void run(String response);

    public int join();
}
