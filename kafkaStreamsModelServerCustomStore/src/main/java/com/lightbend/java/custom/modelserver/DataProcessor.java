package com.lightbend.java.custom.modelserver;

import com.lightbend.model.Winerecord;
import com.lightbend.java.configuration.kafka.ApplicationKafkaParameters;
import com.lightbend.java.custom.modelserver.store.ModelStateStore;
import com.lightbend.java.model.ModelServingInfo;
import org.apache.kafka.streams.processor.AbstractProcessor;
import org.apache.kafka.streams.processor.ProcessorContext;

import java.util.Objects;
import java.util.Optional;

/**
 * Created by boris on 7/12/17.
 * used
 * https://github.com/bbejeck/kafka-streams/blob/master/src/main/java/bbejeck/processor/stocks/StockSummaryProcessor.java
 */
public class DataProcessor extends AbstractProcessor<byte[], Optional<Winerecord.WineRecord>> {

    private ModelStateStore modelStore;

    @Override
    public void process(byte[] key, Optional<Winerecord.WineRecord> dataRecord) {
        if(modelStore.getNewModel() != null){
            // update the model
            if(modelStore.getCurrentModel() != null)
                modelStore.getCurrentModel().cleanup();
            modelStore.setCurrentModel(modelStore.getNewModel());
            modelStore.setCurrentServingInfo(new ModelServingInfo(modelStore.getNewServingInfo().getName(),
                    modelStore.getNewServingInfo().getDescription(), System.currentTimeMillis()));
            modelStore.setNewServingInfo(null);
            modelStore.setNewModel(null);
        }
        // Actually score
        if(modelStore.getCurrentModel() == null) {
            // No model currently
            System.out.println("No model available - skipping");
        }
        else{
            // Score the model
            long start = System.currentTimeMillis();
            double quality = (double) modelStore.getCurrentModel().score(dataRecord.get());
            long duration = System.currentTimeMillis() - start;
            modelStore.getCurrentServingInfo().update(duration);
            System.out.println("Calculated quality - " + quality + " in " + duration + "ms");
         }

    }

    @Override
    public void init(ProcessorContext context) {
        modelStore = (ModelStateStore) context.getStateStore(ApplicationKafkaParameters.STORE_NAME);
        Objects.requireNonNull(modelStore, "State store can't be null");

    }
}
