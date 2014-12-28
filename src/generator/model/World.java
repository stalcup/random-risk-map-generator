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

import generator.util.Lists;
import generator.util.Maps;
import generator.util.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class World
{
  public List< Continent > continents = Lists.of();
  public Map< Integer, Country > countriesById = Maps.of();
  public List< Edge > edges = Lists.of();
  public int maxRounds;
  public Set< Country > startingCountries = Sets.of();
  public Set< Country > wastelandCountries = Sets.of();

  public Set< Country > collectNeighborCountries( Continent continent )
  {
    Set< Country > neighborCountries = Sets.of();
    for ( Country country : continent.countries )
    {
      neighborCountries.addAll( collectNeighborCountries( country ) );
    }
    return neighborCountries;
  }

  public Set< Country > collectNeighborCountries( Country country )
  {
    Set< Country > neighborCountries = Sets.of();
    for ( Edge edge : edges )
    {
      if ( edge.leftCountry.equals( country ) )
      {
        neighborCountries.add( edge.rightCountry );
      }
      if ( edge.rightCountry.equals( country ) )
      {
        neighborCountries.add( edge.leftCountry );
      }
    }
    return neighborCountries;
  }

  public void printSetup()
  {
    System.out.println( "settings max_rounds " + maxRounds );
    System.out.println( "" );

    System.out.print( "setup_map super_regions" );
    for ( Continent continent : continents )
    {
      System.out.print( " " + continent.id + " " + continent.bonus );
    }
    System.out.println( "" );
    System.out.println( "" );

    System.out.print( "setup_map regions" );
    for ( Continent continent : continents )
    {
      for ( Country country : continent.countries )
      {
        System.out.print( " " + country.id + " " + continent.id );
      }
    }
    System.out.println( "" );
    System.out.println( "" );

    System.out.print( "setup_map neighbors" );
    for ( Continent continent : continents )
    {
      for ( Country country : continent.countries )
      {
        System.out.print( " " + country.id );
        Set< Country > neighborCountries = collectNeighborCountries( country );

        String separator = " ";
        for ( Country neighborCountry : neighborCountries )
        {
          System.out.print( separator + neighborCountry.id );
          separator = ",";
        }
      }
    }
    System.out.println( "" );
    System.out.println( "" );

    System.out.print( "setup_map wastelands" );
    for ( Country wastelandCountry : wastelandCountries )
    {
      System.out.print( " " + wastelandCountry.id );
    }
    System.out.println( "" );
    System.out.println( "" );

    System.out.print( "settings starting_regions" );
    for ( Country startingCountry : startingCountries )
    {
      System.out.print( " " + startingCountry.id );
    }
    System.out.println( "" );
    System.out.println( "" );
  }
}
