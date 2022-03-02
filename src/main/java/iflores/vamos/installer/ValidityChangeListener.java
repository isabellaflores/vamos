package iflores.vamos.installer;

@FunctionalInterface
public interface ValidityChangeListener {

    void validityChanged(boolean valid);

}
