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
package generator;

import generator.model.Continent;
import generator.model.Country;
import generator.model.Edge;
import generator.model.World;
import generator.util.Lists;
import generator.util.Maps;
import generator.util.Sets;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Generates a random world with an approximate country count.
 * <p>
 * Works by first creating a detailed (rectangular, not hexagonal) grid of cells, combining groups
 * of grid cells together into variously sized and connected countries and then combining groups of
 * countries together into continents.
 * <p>
 * The implementation is kept simple by not creating id based indexes of countries and edges that
 * would need to be updated after every grid cell merge, but the simplicity comes at the cost of
 * performance.
 */
public class RandomWorldGenerator
{
  private static final double GRID_CELL_COMBINES_PER_COUNTRY = 2.5;
  private static int MAX_CONTINENT_SIZE = 6;
  private static final int MIN_CONTINENT_SIZE = 2;
  private static final double UNUSED_COUNTRY_OVERAGE = 1.12;

  public static World generate( int goalCountryCount )
  {
    RandomWorldGenerator randomWorldGenerator = new RandomWorldGenerator( goalCountryCount );
    return randomWorldGenerator.generate();
  }

  private final Country[][] cellGrid;
  private Set< Country > countriesNotUsedInContinents;
  private final Set< Country > countriesUsedInContinents = Sets.of();
  private final int gridHeight;
  private final int gridWidth;
  private final World world = new World();

  private RandomWorldGenerator( int goalCountryCount )
  {
    int gridCellCount = ( int ) ( goalCountryCount * GRID_CELL_COMBINES_PER_COUNTRY * UNUSED_COUNTRY_OVERAGE );
    gridWidth = ( int ) Math.sqrt( gridCellCount );
    gridHeight = gridWidth + 1;
    cellGrid = new Country[gridWidth][gridHeight];
  }

  private void assignFinalCountryIds()
  {
    Set< Country > continentCountries = collectCountriesInContinents();
    int id = 0;
    Iterator< Country > iterator = continentCountries.iterator();
    while ( iterator.hasNext() )
    {
      Country country = iterator.next();
      country.id = id;
      id++;

      world.countriesById.put( country.id, country );
    }
  }

  private void assignTemporaryCountryIds()
  {
    Set< Country > countries = collectAllCountries();
    int id = 0;
    Iterator< Country > iterator = countries.iterator();
    while ( iterator.hasNext() )
    {
      Country country = iterator.next();
      country.id = id;
      id++;
    }
  }

  private void calculateMaxRounds()
  {
    world.maxRounds = ( int ) ( collectCountriesInContinents().size() * 2.5 );
  }

  /**
   * Seems to generate about the same bonus randomness, size, and quantity of 0 bonus continents as
   * the Warlight generation.
   */
  private int chooseContinentBonus( Continent continent )
  {
    return ( int ) ( ( continent.countries.size() - 1 ) + ( -1.5 + ( Math.random() * 4 ) ) );
  }

  private void chooseStartingCountries()
  {
    for ( Continent continent : world.continents )
    {
      Country startingCountry = Lists.chooseRandom( continent.countries );
      world.startingCountries.add( startingCountry );
    }

    world.wastelandCountries.removeAll( world.startingCountries );
  }

  private void chooseWastelandCountries()
  {
    Set< Country > continentCountries = collectCountriesInContinents();

    int wastelandCount = ( int ) ( world.continents.size() / 2.4 );
    for ( int i = 0; i < wastelandCount; i++ )
    {
      Country wastelandCountry = Lists.chooseRandom( continentCountries );
      world.wastelandCountries.add( wastelandCountry );
    }
  }

  private Set< Country > collectAllCountries()
  {
    Set< Country > countries = Sets.of();
    for ( Edge edge : world.edges )
    {
      countries.add( edge.leftCountry );
      countries.add( edge.rightCountry );
    }
    return countries;
  }

  private Map< Country, Continent > collectContinentsByCountry()
  {
    Map< Country, Continent > continentsByCountry = Maps.of();
    for ( Continent continent : world.continents )
    {
      for ( Country country : continent.countries )
      {
        continentsByCountry.put( country, continent );
      }
    }
    return continentsByCountry;
  }

  private Set< Country > collectCountriesInContinents()
  {
    Set< Country > countries = new LinkedHashSet< Country >();
    for ( Continent continent : world.continents )
    {
      countries.addAll( continent.countries );
    }
    return countries;
  }

  private void combineCountryCells()
  {
    double combineCount = ( ( gridWidth * gridHeight ) * ( GRID_CELL_COMBINES_PER_COUNTRY - 1 ) )
        / GRID_CELL_COMBINES_PER_COUNTRY;
    for ( int i = 0; i < combineCount; i++ )
    {
      Country barCountry = Lists.chooseRandom( collectAllCountries() );
      Country fooCountry = Lists.chooseRandom( world.collectNeighborCountries( barCountry ) );

      if ( fooCountry.equals( barCountry ) )
      {
        i--;
        continue;
      }

      mergeIntoCountry( fooCountry, barCountry );
      removeDuplicateAndCircularEdges();
    }
  }

  private World generate()
  {
    initCountries();
    initEdges();

    combineCountryCells();
    assignTemporaryCountryIds();
    makeContinents();
    removeUnusedEdges();
    removeUnconnectedContinents();

    assignFinalCountryIds();
    calculateMaxRounds();

    chooseWastelandCountries();
    chooseStartingCountries();

    printCountryLayout();
    printContinentLayout();
    printContinentSummary();

    return world;
  }

  private void initCountries()
  {
    for ( int x = 0; x < gridWidth; x++ )
    {
      for ( int y = 0; y < gridHeight; y++ )
      {
        cellGrid[x][y] = new Country( x, y );
      }
    }
  }

  private void initEdges()
  {
    for ( int x = 0; x < gridWidth; x++ )
    {
      for ( int y = 0; y < gridHeight; y++ )
      {
        int nextX = x + 1;
        int nextY = y + 1;

        if ( nextX < gridWidth )
        {
          world.edges.add( new Edge( cellGrid[x][y], cellGrid[nextX][y] ) );
        }
        if ( nextY < gridHeight )
        {
          world.edges.add( new Edge( cellGrid[x][y], cellGrid[x][nextY] ) );
        }
      }
    }
  }

  private void makeContinents()
  {
    countriesNotUsedInContinents = collectAllCountries();
    countriesNotUsedInContinents.removeAll( countriesUsedInContinents );

    while ( !countriesNotUsedInContinents.isEmpty() )
    {
      Country continentSeedCountry = Lists.chooseRandom( countriesNotUsedInContinents );
      Continent continent = new Continent();
      continent.countries.add( continentSeedCountry );
      countriesUsedInContinents.add( continentSeedCountry );
      do
      {
        Set< Country > unusedNeighborCountries = world.collectNeighborCountries( continent );
        unusedNeighborCountries.removeAll( countriesUsedInContinents );

        if ( unusedNeighborCountries.isEmpty() )
        {
          break;
        }

        Country nextCountry = Lists.chooseRandom( unusedNeighborCountries );
        continent.countries.add( nextCountry );
        countriesUsedInContinents.add( nextCountry );

        if ( continent.countries.size() > MAX_CONTINENT_SIZE )
        {
          break;
        }
      } while ( Math.random() < 0.7 );

      if ( continent.countries.size() > ( MIN_CONTINENT_SIZE - 1 ) )
      {
        continent.id = ( world.continents.size() + 1 );
        continent.bonus = chooseContinentBonus( continent );
        world.continents.add( continent );
      }

      countriesNotUsedInContinents = collectAllCountries();
      countriesNotUsedInContinents.removeAll( countriesUsedInContinents );
    }

    countriesNotUsedInContinents = collectAllCountries();
    countriesNotUsedInContinents.removeAll( collectCountriesInContinents() );
  }

  private void mergeIntoCountry( Country country, Country intoCountry )
  {
    for ( Edge edge : world.edges )
    {
      if ( edge.leftCountry.equals( country ) )
      {
        edge.leftCountry = intoCountry;
      }
      if ( edge.rightCountry.equals( country ) )
      {
        edge.rightCountry = intoCountry;
      }

      for ( int x = 0; x < gridWidth; x++ )
      {
        for ( int y = 0; y < gridHeight; y++ )
        {
          if ( cellGrid[x][y].equals( country ) )
          {
            cellGrid[x][y] = intoCountry;
          }
        }
      }
    }
  }

  private void printContinentLayout()
  {
    Map< Country, Continent > continentsByCountry = collectContinentsByCountry();

    System.out.println( "" );
    System.out.println( "Continent view:" );
    System.out.println( "// All of the grid cells that contain the same "
        + "number are the space filled by that continent #." );

    for ( int y = 0; y < gridHeight; y++ )
    {
      for ( int x = 0; x < gridWidth; x++ )
      {
        Country country = cellGrid[x][y];
        if ( !continentsByCountry.containsKey( country ) )
        {
          System.out.print( "   " );
          continue;
        }
        Continent continent = continentsByCountry.get( country );
        String name = continent.id + "";
        System.out.print( name + ( name.length() == 1 ? "  " : " " ) );
      }
      System.out.println( "" );
    }

    System.out.println();
  }

  private void printContinentSummary()
  {
    for ( Continent continent : world.continents )
    {
      System.out.println( "Continent #" + continent.id + " contains " + continent.countries.size()
          + " countries and has a bonus of " + continent.bonus );
    }
    System.out.println();
  }

  private void printCountryLayout()
  {
    System.out.println( "" );
    System.out.println( "Country view:" );
    System.out.println( "// All of the grid cells that contain the same "
        + "number are the space filled by that country #." );

    for ( int y = 0; y < gridHeight; y++ )
    {
      for ( int x = 0; x < gridWidth; x++ )
      {
        String name = cellGrid[x][y].id + "";
        System.out.print( name + ( name.length() == 1 ? "  " : " " ) );
      }
      System.out.println( "" );
    }
  }

  private void removeDuplicateAndCircularEdges()
  {
    world.edges = Lists.of( Sets.of( world.edges ) );

    Iterator< Edge > iterator = world.edges.iterator();
    while ( iterator.hasNext() )
    {
      Edge edge = iterator.next();
      if ( edge.leftCountry.equals( edge.rightCountry ) )
      {
        iterator.remove();
      }
    }
  }

  /**
   * Finds and removes any continent that is not connected to any other continent.
   * <p>
   * If there is a pair of continents that connect to each other but do not connect to the rest of
   * the world they will not be found and removed. This occurs in about 1 out of every 10,000
   * worlds.
   */
  private void removeUnconnectedContinents()
  {
    Iterator< Continent > iterator = world.continents.iterator();
    while ( iterator.hasNext() )
    {
      Continent continent = iterator.next();

      Set< Country > neighborCountries = world.collectNeighborCountries( continent );

      // If the expanded neighbor set is the same as the contained countries.
      if ( neighborCountries.size() == continent.countries.size() )
      {
        // Then the continent is disconnected.
        iterator.remove();
      }
    }
  }

  private void removeUnusedEdges()
  {
    Iterator< Edge > iterator = world.edges.iterator();
    while ( iterator.hasNext() )
    {
      Edge edge = iterator.next();
      if ( countriesNotUsedInContinents.contains( edge.leftCountry )
          || countriesNotUsedInContinents.contains( edge.rightCountry ) )
      {
        iterator.remove();
      }
    }
  }
}
