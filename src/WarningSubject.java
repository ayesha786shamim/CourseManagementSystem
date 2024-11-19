import java.util.ArrayList;
import java.util.List;

public class WarningSubject {
    private List<WarningObserver> observers = new ArrayList<>();

    public void addObserver(WarningObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(WarningObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(String message) {
        for (WarningObserver observer : observers) {
            observer.update(message);
        }
    }
}
