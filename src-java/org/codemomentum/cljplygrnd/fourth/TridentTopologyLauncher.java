package org.codemomentum.cljplygrnd.fourth;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.LocalDRPC;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import org.codemomentum.cljplygrnd.first.RandomSentenceSpout;
import storm.trident.TridentState;
import storm.trident.TridentTopology;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.operation.builtin.Count;
import storm.trident.operation.builtin.FilterNull;
import storm.trident.operation.builtin.MapGet;
import storm.trident.operation.builtin.Sum;
import storm.trident.testing.MemoryMapState;
import storm.trident.tuple.TridentTuple;


public class TridentTopologyLauncher {

    public static class Split extends BaseFunction {
        @Override
        public void execute(TridentTuple tuple, TridentCollector collector) {
            String sentence = tuple.getString(0);
            for(String word: sentence.split(" ")) {
                collector.emit(new Values(word));
            }
        }
    }

    public static StormTopology buildTopology(LocalDRPC drpc) {


        TridentTopology topology = new TridentTopology();
        TridentState wordCounts =
                topology.newStream("spout1", new RandomSentenceSpout())
                        .parallelismHint(16)
                        .each(new Fields("sentence"), new Split(), new Fields("word"))
                        //stream is grouped by the "word" field.
                        .groupBy(new Fields("word"))
                        //each group is persistently aggregated using the Count aggregator
                        .persistentAggregate(new MemoryMapState.Factory(),     //the word counts are kept in memory,
                                                                               // but this can be trivially swapped to use Memcached, Cassandra, or any other persistent store
                                new Count(), new Fields("count"))
                        .parallelismHint(16);
        //the values stored by persistentAggregate represents the aggregation of all batches ever emitted by the stream.
        //The persistentAggregate method transforms a Stream into a TridentState object. In this case the TridentState object represents all the word counts.
        // We will use this TridentState object to implement the distributed query portion of the computation.


        topology.newDRPCStream("words", drpc)
                .each(new Fields("args"), new Split(), new Fields("word"))
                .groupBy(new Fields("word"))
                .stateQuery(wordCounts, new Fields("word"), new MapGet(), new Fields("count"))
                .each(new Fields("count"), new FilterNull())
                .aggregate(new Fields("count"), new Sum(), new Fields("sum"))
        ;
        return topology.build();
    }

    public static void main(String[] args) throws Exception {
        Config conf = new Config();
        conf.setMaxSpoutPending(20);
        conf.setNumWorkers(1);
        if(args.length==0) {
            LocalDRPC drpc = new LocalDRPC();
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("wordCounter", conf, buildTopology(drpc));
            for(int i=0; i<100; i++) {
                System.out.println("DRPC RESULT: " + drpc.execute("words", "cat the dog jumped"));
                Thread.sleep(1000);
            }
        } else {
            conf.setNumWorkers(3);
            StormSubmitter.submitTopology(args[0], conf, buildTopology(null));
        }
    }
}
