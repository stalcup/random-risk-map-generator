/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2014 MrTaco
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package generator.model;

public class Country
{
  public int id = -1;
  private final int originalGridCellX;
  private final int originalGridCellY;

  public Country( int gridCellX, int gridCellY )
  {
    originalGridCellX = gridCellX;
    originalGridCellY = gridCellY;
  }

  @Override
  public boolean equals( Object obj )
  {
    if ( this == obj )
    {
      return true;
    }
    if ( obj == null )
    {
      return false;
    }
    if ( getClass() != obj.getClass() )
    {
      return false;
    }
    Country other = ( Country ) obj;
    if ( originalGridCellX != other.originalGridCellX )
    {
      return false;
    }
    if ( originalGridCellY != other.originalGridCellY )
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = ( prime * result ) + originalGridCellX;
    result = ( prime * result ) + originalGridCellY;
    return result;
  }
}
