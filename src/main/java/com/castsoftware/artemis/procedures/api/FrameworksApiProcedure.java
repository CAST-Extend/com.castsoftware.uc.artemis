/*
 * Copyright (C) 2020  Hugo JOBY
 *
 *  This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty ofnMERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNUnLesser General Public License v3 for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public v3 License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

package com.castsoftware.artemis.procedures.api;

import com.castsoftware.artemis.controllers.api.FrameworkController;
import com.castsoftware.artemis.database.Neo4jAL;
import com.castsoftware.artemis.datasets.FrameworkNode;
import com.castsoftware.artemis.exceptions.ProcedureException;
import com.castsoftware.artemis.exceptions.neo4j.Neo4jBadNodeFormatException;
import com.castsoftware.artemis.exceptions.neo4j.Neo4jConnectionError;
import com.castsoftware.artemis.exceptions.neo4j.Neo4jQueryException;
import com.castsoftware.artemis.results.FrameworkResult;
import com.castsoftware.artemis.results.LongResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.List;
import java.util.stream.Stream;

public class FrameworksApiProcedure {
  @Context public GraphDatabaseService db;

  @Context public Transaction transaction;

  @Context public Log log;


  @Procedure(value = "artemis.api.add.framework", mode = Mode.WRITE)
  @Description(
      "artemis.api.add.framework(String Name, String DiscoveryDate, String Location, String Description, String Type, String Category, String InternalType) - Add a framework")
  public Stream<FrameworkResult> addFramework(
      @Name(value = "Name") String name,
      @Name(value = "DiscoveryDate", defaultValue = "") String discoveryDate,
      @Name(value = "Location", defaultValue = "") String location,
      @Name(value = "Description", defaultValue = "") String description,
      @Name(value = "Type", defaultValue = "") String type,
      @Name(value = "Category", defaultValue = "") String category,
      @Name(value = "InternalType", defaultValue = "") String internalType)
      throws ProcedureException {

    try {
      Neo4jAL nal = new Neo4jAL(db, transaction, log);
      FrameworkNode addedFramework =
          FrameworkController.addFramework(
              nal, name, discoveryDate, location, description, type, category, internalType);

      return Stream.of(new FrameworkResult(addedFramework));
    } catch (Exception | Neo4jConnectionError | Neo4jQueryException e) {
      ProcedureException ex = new ProcedureException(e);
      log.error("An error occurred while executing the procedure", e);
      throw ex;
    }
  }

  @Procedure(value = "artemis.api.update.framework", mode = Mode.WRITE)
  @Description(
      "artemis.api.update.framework(String Name, String DiscoveryDate, String Location, String Description, String Type, String Category, String InternalType, Long NumberOfDetection, Double PercentageOfDetection ) - Update a framework using its name")
  public Stream<FrameworkResult> updateFramework(
      @Name(value = "Name") String name,
      @Name(value = "DiscoveryDate") String discoveryDate,
      @Name(value = "Location") String location,
      @Name(value = "Description") String description,
      @Name(value = "Type") String type,
      @Name(value = "Category") String category,
      @Name(value = "InternalType") String internalType,
      @Name(value = "NumberOfDetection", defaultValue = "0") Long numberOfDetection,
      @Name(value = "PercentageOfDetection", defaultValue = "0") Double percentageOfDetection)
      throws ProcedureException {

    try {
      Neo4jAL nal = new Neo4jAL(db, transaction, log);
      FrameworkNode addedFramework =
          FrameworkController.updateFramework(
              nal,
              name,
              discoveryDate,
              location,
              description,
              type,
              category,
              numberOfDetection,
              percentageOfDetection,
              internalType);
      return Stream.of(new FrameworkResult(addedFramework));
    } catch (Exception
        | Neo4jConnectionError
        | Neo4jQueryException
        | Neo4jBadNodeFormatException e) {
      ProcedureException ex = new ProcedureException(e);
      log.error("An error occurred while executing the procedure", e);
      throw ex;
    }
  }

  @Procedure(value = "artemis.api.find.framework", mode = Mode.WRITE)
  @Description(
          "artemis.api.find.framework(String Name, Optional String InternalType) - Find a framework using its name")
  public Stream<FrameworkResult> findFramework(
          @Name(value = "Name") String name,
          @Name(value = "InternalType", defaultValue = "") String internalType ) throws ProcedureException {

    try {
      Neo4jAL nal = new Neo4jAL(db, transaction, log);
      FrameworkNode fn;
      if(internalType.isEmpty()) {
        fn = FrameworkController.findFrameworkByName( nal, name);
      } else {
        fn = FrameworkController.findFrameworkByNameAndType( nal, name, internalType );
      }

      if(fn == null) return Stream.of();

      return Stream.of(new FrameworkResult(fn));

    } catch (Exception
            | Neo4jConnectionError
            | Neo4jQueryException
            | Neo4jBadNodeFormatException e) {
      ProcedureException ex = new ProcedureException(e);
      log.error("An error occurred while executing the procedure", e);
      throw ex;
    }
  }


  @Procedure(value = "artemis.api.find.framework.name.contains", mode = Mode.WRITE)
  @Description(
          "artemis.api.find.framework.name.contains(String Name, Long limit) - Get the 10 first frameworks containing a certains string in the name")
  public Stream<FrameworkResult> findFrameworkNameContains(
          @Name(value = "Name") String name,
          @Name(value = "Limit") Long limit ) throws ProcedureException {

    try {
      Neo4jAL nal = new Neo4jAL(db, transaction, log);
      List<FrameworkNode> nodeList = FrameworkController.findFrameworkNameContains( nal, name, limit);;

      return nodeList.stream().map(FrameworkResult::new);

    } catch (Exception
            | Neo4jConnectionError
            | Neo4jQueryException e) {
      ProcedureException ex = new ProcedureException(e);
      log.error("An error occurred while executing the procedure", e);
      throw ex;
    }
  }

  @Procedure(value = "artemis.api.get.framework.batch", mode = Mode.WRITE)
  @Description(
          "artemis.api.get.framework.batch(Long StartIndex, Long EndIndex, Optional String InternalType) - Get the list of Framework using a start and a end index")
  public Stream<FrameworkResult> getBatchedFrameworks(
          @Name(value = "StartIndex") Long startIndex,
          @Name(value = "EndIndex") Long stopIndex,
          @Name(value = "InternalType", defaultValue = "") String internalType ) throws ProcedureException {

    try {
      Neo4jAL nal = new Neo4jAL(db, transaction, log);
      List<FrameworkNode> frameworkNodeList;
      if (internalType.isEmpty()) {
        frameworkNodeList = FrameworkController.getBatchedFrameworks(nal, startIndex, stopIndex);
      } else {
        frameworkNodeList = FrameworkController.getBatchedFrameworksByType(nal, startIndex, stopIndex, internalType);
      }

      return frameworkNodeList.stream().map(FrameworkResult::new);
    } catch (Exception
            | Neo4jConnectionError
            | Neo4jQueryException e) {
      ProcedureException ex = new ProcedureException(e);
      log.error("An error occurred while executing the procedure", e);
      throw ex;
    }
  }

  @Procedure(value = "artemis.api.get.framework.number", mode = Mode.WRITE)
  @Description(
          "artemis.api.get.framework.number(Optional String InternalType) - Get the number of framework in the database. Optional filter on internal type ")
  public Stream<LongResult> getFrameworksNumber(
          @Name(value = "InternalType", defaultValue = "") String internalType ) throws ProcedureException {

    try {
      Neo4jAL nal = new Neo4jAL(db, transaction, log);
      Long numFramework;

      if (internalType.isEmpty()) {
        numFramework = FrameworkController.getNumFrameworks(nal);
      } else {
        numFramework = FrameworkController.getNumFrameworksByInternalType(nal, internalType);
      }

      return Stream.of(new LongResult(numFramework));
    } catch (Exception
            | Neo4jConnectionError
            | Neo4jQueryException e) {
      ProcedureException ex = new ProcedureException(e);
      log.error("An error occurred while executing the procedure", e);
      throw ex;
    }
  }

  @Procedure(value = "artemis.api.get.framework.candidate", mode = Mode.WRITE)
  @Description(
          "artemis.api.get.framework.candidate(String application, String language) - Get the number of candidate on an application with a specific language")
  public Stream<LongResult> getCandidateNumber(
          @Name(value = "Application") String application,
          @Name(value = "Language") String language) throws ProcedureException {

    try {
      Neo4jAL nal = new Neo4jAL(db, transaction, log);
      Long numCandidate =  FrameworkController.getNumCandidateByLanguage(nal, application, language);
      return Stream.of(new LongResult(numCandidate));
    } catch (Exception
            | Neo4jConnectionError
            | Neo4jQueryException e) {
      ProcedureException ex = new ProcedureException(e);
      log.error("An error occurred while executing the procedure", e);
      throw ex;
    }
  }

}
