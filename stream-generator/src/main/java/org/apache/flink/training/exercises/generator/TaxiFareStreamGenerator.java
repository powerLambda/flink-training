/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.training.exercises.generator;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.training.exercises.common.datatypes.TaxiFare;
import org.apache.flink.training.exercises.common.sources.TaxiFareGenerator;
import org.apache.flink.training.exercises.common.utils.ExerciseBase;

import java.util.Properties;

public class TaxiFareStreamGenerator extends ExerciseBase {

    /**
     * Main method.
     *
     * @throws Exception which occurs during job execution.
     */
    public static void main(String[] args) throws Exception {

        // set up streaming execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.setParallelism(ExerciseBase.parallelism);

        // start the data generator and transform to json string
        DataStream<String> fares = env.addSource(fareSourceOrTest(new TaxiFareGenerator()))
                .keyBy((TaxiFare fare) -> fare.driverId)
                .map((MapFunction<TaxiFare, String>) fare -> fare.toJSONString());

        //define sink write to kafka sink
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", args[0]);
        FlinkKafkaProducer<String> taxiFareProducer = new FlinkKafkaProducer<>(
                args[1],                  // target topic
                new SimpleStringSchema(),    // serialization schema
                properties                  // producer config
        );

        fares.addSink(taxiFareProducer);


        // execute the transformation pipeline
        env.execute("TaxiFareStreamGenerator (java)");
    }

}
