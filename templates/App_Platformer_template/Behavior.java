package ${PROJECT_PACKAGE_NAME};

  /**
   * Define a Behavior to be applied to any object T.
   */
  public interface Behavior<T> {
    /**
     * Apply the Behavior to the T object.
     * 
     * @param o T object instance to apply Behavior to.
     */
    void apply(T o);
  }