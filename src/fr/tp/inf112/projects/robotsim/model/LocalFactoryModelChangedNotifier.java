package fr.tp.inf112.projects.robotsim.model;
import fr.tp.inf112.projects.canvas.controller.Observer;

import java.util.ArrayList;
import java.util.List;

public class LocalFactoryModelChangedNotifier implements FactoryModelChangedNotifier {
    private final List<Observer> observers;

    public LocalFactoryModelChangedNotifier() {
        this.observers = new ArrayList<>();
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : this.observers) {
            observer.modelChanged();
        }
    }

    @Override
    public boolean addObserver(Observer observer) {
        return observers.add(observer);
    }

    @Override
    public boolean removeObserver(Observer observer) {
        return observers.remove(observer);
    }
}
