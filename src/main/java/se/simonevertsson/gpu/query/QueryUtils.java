package se.simonevertsson.gpu.query;

import com.nativelibs4java.opencl.CLEvent;
import org.bridj.Pointer;
import se.simonevertsson.gpu.dictionary.QueryIdDictionary;
import se.simonevertsson.gpu.kernel.QueryKernels;
import se.simonevertsson.gpu.query.relationship.join.PossibleSolutions;
import se.simonevertsson.runner.AliasDictionary;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QueryUtils {

  public static int[] gatherCandidateArray(Pointer<Boolean> candidateIndicatorsPointer, int dataNodeCount, int nodeId) {
    int[] prefixScanArray = new int[dataNodeCount];
    int candidateCount = 0;
    int offset = dataNodeCount * nodeId;
    if (candidateIndicatorsPointer.get(offset + 0)) {
      candidateCount++;
    }

    for (int i = 1; i < dataNodeCount; i++) {
      int nextElement = candidateIndicatorsPointer.get(offset + i - 1) ? 1 : 0;
      prefixScanArray[i] = prefixScanArray[i - 1] + nextElement;
      if (candidateIndicatorsPointer.get(offset + i)) {
        candidateCount++;
      }
    }

    int[] candidateArray = new int[candidateCount];

    for (int i = 0; i < dataNodeCount; i++) {
      if (candidateIndicatorsPointer.get(offset + i)) {
        candidateArray[prefixScanArray[i]] = i;
      }
    }
    return candidateArray;
  }


  public static boolean[] pointerBooleanToArray(Pointer<Boolean> pointer, int size) {
    boolean[] result = new boolean[size];
    int i = 0;
    for (boolean element : pointer) {
      result[i] = element;
      i++;
    }
    return result;
  }

  public static int[] pointerIntegerToArray(Pointer<Integer> pointer, int size) {
    int[] result = new int[size];
    int i = 0;
    for (int element : pointer) {
      result[i] = element;
      i++;
    }
    return result;
  }

  public static int[] generatePrefixScanArray(Pointer<Integer> bufferPointer, int bufferSize) {
    int totalElementCount = 0;
    int[] prefixScanArray = new int[bufferSize + 1];
    for (int i = 0; i < bufferSize; i++) {
      prefixScanArray[i] = totalElementCount;
      totalElementCount += bufferPointer.get(i);
    }
    prefixScanArray[bufferSize] = totalElementCount;
    return prefixScanArray;
  }

  public static int[] generatePrefixScanArrayFromBooleans(Pointer<Boolean> bufferPointer, int bufferSize) {
    int totalElementCount = 0;
    int[] prefixScanArray = new int[bufferSize + 1];
    for (int i = 0; i < bufferSize; i++) {
      prefixScanArray[i] = totalElementCount;
      totalElementCount += bufferPointer.get(i) ? 1 : 0;
    }
    prefixScanArray[bufferSize] = totalElementCount;
    return prefixScanArray;
  }


  public static List<QuerySolution> generateQuerySolutions(QueryKernels queryKernels, QueryContext queryContext, PossibleSolutions solution) {
    ArrayList<QuerySolution> results = new ArrayList<QuerySolution>();
    List<Map.Entry<String, Integer>> solutionElements = null;

    if (solution != null) {
      AliasDictionary aliasDictionary = queryContext.queryGraph.aliasDictionary;
      QueryIdDictionary queryGraphQueryIdDictionary = queryContext.gpuQuery.getNodeIdDictionary();
      QueryIdDictionary dataGraphQueryIdDictionary = queryContext.gpuData.getNodeIdDictionary();
      Pointer<Integer> solutionsPointer = solution.getSolutionElements().read(queryKernels.queue);
      int solutionCount = (int) (solution.getSolutionElements().getElementCount() / queryContext.queryNodeCount);


      for (int i = 0; i < solutionCount * queryContext.queryNodeCount; i++) {
        if (i % queryContext.queryNodeCount == 0) {
          solutionElements = new ArrayList<>();
        }
        int queryGraphQueryId = i % queryContext.queryNodeCount;
        int queryGraphId = (int) queryGraphQueryIdDictionary.getId(queryGraphQueryId);
        int solutionElementQueryId = solutionsPointer.get(i);
        int solutionElementId = (int) dataGraphQueryIdDictionary.getId(solutionElementQueryId);

        String alias = aliasDictionary.getAliasForId(queryGraphId);

        solutionElements.add(new AbstractMap.SimpleEntry<String, Integer>(alias, solutionElementId));
        if (i % queryContext.queryNodeCount == queryContext.queryNodeCount - 1) {
          results.add(new QuerySolution(queryContext.queryGraph, solutionElements));
        }
      }
    }

    return results;
  }

  public static double getEventRunTime(CLEvent event) {
    return (double) (event.getProfilingCommandEnd() - event.getProfilingCommandStart()) / 10e6;
  }
}