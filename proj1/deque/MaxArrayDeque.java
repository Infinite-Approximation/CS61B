package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> comparator;
    public MaxArrayDeque(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    public T max() {
        if (this.size() == 0) {
            return null;
        }
        T maxT = this.get(0);
        for (int i = 1; i < this.size(); i++) {
            if (comparator.compare(maxT, this.get(i)) < 0) {
                maxT = this.get(i);
            }
        }
        return maxT;
    }

    public T max(Comparator<T> comp) {
        if (this.size() == 0) {
            return null;
        }
        T maxT = this.get(0);
        for (int i = 1; i < this.size(); i++) {
            if (comp.compare(maxT, this.get(i)) < 0) {
                maxT = this.get(i);
            }
        }
        return maxT;
    }
}
