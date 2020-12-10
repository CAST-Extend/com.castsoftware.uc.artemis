package com.castsoftware.artemis.procedures;

import com.castsoftware.artemis.controllers.DetectionController;
import com.castsoftware.artemis.database.Neo4jAL;
import com.castsoftware.artemis.exceptions.ProcedureException;
import com.castsoftware.artemis.exceptions.file.MissingFileException;
import com.castsoftware.artemis.exceptions.neo4j.Neo4jBadRequestException;
import com.castsoftware.artemis.exceptions.neo4j.Neo4jConnectionError;
import com.castsoftware.artemis.exceptions.neo4j.Neo4jNoResult;
import com.castsoftware.artemis.exceptions.neo4j.Neo4jQueryException;
import com.castsoftware.artemis.exceptions.nlp.NLPIncorrectConfigurationException;
import com.castsoftware.artemis.results.OutputMessage;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

public class DetectionProcedure {


    @Context
    public GraphDatabaseService db;

    @Context
    public Transaction transaction;

    @Context
    public Log log;

    @Procedure(value = "artemis.launchDetection", mode = Mode.WRITE)
    @Description("artemis.launchDetection(String ApplicationContext, String Language) - Launch Detection for a specific language")
    public Stream<OutputMessage> launchDetection(@Name(value = "ApplicationContext") String applicationContext,
                                                 @Name(value="Language", defaultValue = "") String language) throws ProcedureException {

        try {
            Neo4jAL nal = new Neo4jAL(db, transaction, log);

            List<String> detectedFrameworks = DetectionController.launchDetection(nal, applicationContext, language);

            return detectedFrameworks.stream().map(OutputMessage::new);
        } catch (Exception | Neo4jConnectionError | Neo4jQueryException | MissingFileException | NLPIncorrectConfigurationException e) {
            ProcedureException ex = new ProcedureException(e);
            log.error("An error occurred while executing the procedure", e);
            throw ex;
        }

    }

    @Procedure(value = "artemis.trainModel", mode = Mode.WRITE)
    @Description("artemis.trainModel() - Launch Detection for a specific language")
    public Stream<OutputMessage> trainModel() throws ProcedureException {

        try {
            Neo4jAL nal = new Neo4jAL(db, transaction, log);

            Instant start = Instant.now();
            DetectionController.trainArtemis(nal);
            Instant stop = Instant.now();

            String message = String.format("Model was trained in '%d' milliseconds.", Duration.between(start, stop).toMillis());

            return Stream.of(new OutputMessage(message));
        } catch (Exception | Neo4jConnectionError | NLPIncorrectConfigurationException e) {
            ProcedureException ex = new ProcedureException(e);
            log.error("An error occurred while executing the procedure", e);
            throw ex;
        }

    }

}