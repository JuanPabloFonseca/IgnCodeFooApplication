/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package numberboggle;

/**
 *
 * @author Juan Pablo
 */
public class Coordinate<X,Y> {

  private final X first;
  private final Y second;

  public Coordinate(X first, Y second) {
    this.first = first;
    this.second = second;
  }

  public X getFirst() { 
      return first; 
  }
  public Y getSecond() { 
      return second; 
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Coordinate))
        return false;
    Coordinate c = (Coordinate) o;
    return this.first.equals(c.getFirst()) &&
           this.second.equals(c.getSecond());
  }

}
