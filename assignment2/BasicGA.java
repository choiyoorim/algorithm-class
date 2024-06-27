import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class BasicGA {
    static Graph GAGraph;

    public static void main(String[] args) throws IOException {
        long beforeTime = System.currentTimeMillis();
        BufferedReader br = new BufferedReader(new FileReader("./maxcut.in"));
        FileWriter fw = new FileWriter("./maxcut.out");
        int nodeCount, edgeCount;
        StringTokenizer st = new StringTokenizer(br.readLine());
        nodeCount = Integer.parseInt(st.nextToken());
        edgeCount = Integer.parseInt(st.nextToken());
        HashMap<Integer, ArrayList<Edge>> edges = new HashMap<>();

        for (int i = 0; i < edgeCount; i++) {
            StringTokenizer st1 = new StringTokenizer(br.readLine());
            int start = Integer.parseInt(st1.nextToken());
            int end = Integer.parseInt(st1.nextToken());
            int weight = Integer.parseInt(st1.nextToken());
            if (!edges.containsKey(start)) {
                edges.put(start, new ArrayList<>());
            }
            if (!edges.containsKey(end)) {
                edges.put(end, new ArrayList<>());
            }
            edges.get(start).add(new Edge(start, end, weight));
            edges.get(end).add(new Edge(end, start, weight));
        }

        GAGraph = new Graph(nodeCount, edges);

        int population = 500;
        int offspringNumber = (int) (population * 1);

        GA NewGA = new GA(population, offspringNumber, GAGraph);

        NewGA.generatePool(nodeCount);
        Chromosome[] offspring = new Chromosome[offspringNumber];
        int j = 0;

        while (true) {
            for (int i = 0; i < offspringNumber; i++) {
                // 1. Selection
                Chromosome[] selected = new Chromosome[2];
                selected[0] = NewGA.selectionByTournament();
                selected[1] = NewGA.selectionByTournament();

                // 2. CrossOver
                Chromosome childChromosome;
                childChromosome = NewGA.uniformCrossOver(selected[0], selected[1]);

                // 3. Mutation
                NewGA.mutation(childChromosome);

                offspring[i] = childChromosome;


            }

            // 4. Replace
            NewGA.replace(offspring);

            long afterTime = System.currentTimeMillis();
            long secDiffTime = (afterTime - beforeTime);
            if (secDiffTime > 178000) break;

            if(j >= 100) break;
            j += 1;

//            if (j >= 70) break;
        }

        SortByParanoidQuickSort.paranoidQuickSort(NewGA.chromosomes, 0, NewGA.chromosomes.length - 1);
        String bestResult = NewGA.chromosomes[0].chromosome;
        for (int i = 0; i < bestResult.length(); i++) {
            if (bestResult.charAt(i) == '1') fw.write(i + 1 + " ");
            else continue;
        }

        fw.write("\n");

        br.close();
        fw.close();
    }
}

class Edge {
    int start, end, weight;

    Edge(int start, int end, int weight) {
        this.start = start;
        this.end = end;
        this.weight = weight;
    }
}

class Graph {
    int nodes;
    HashMap<Integer, ArrayList<Edge>> edges;

    Graph(int nodes, HashMap<Integer, ArrayList<Edge>> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public ArrayList<Edge> getGraph(int start) {
        return edges.get(start);
    }
}


class GA {
    int population, offspringNumber;
    Chromosome[] chromosomes;
    Graph GAGraph;

    static double mutationStrength = 0.015;

    GA(int population, int offspringNumber, Graph GAGraph) {
        this.population = population;
        this.offspringNumber = offspringNumber;
        this.GAGraph = GAGraph;
    }

    public Chromosome generateChromosome(int length) {
        String chromosome = "";
        Chromosome chromosomeObj = new Chromosome();
        for (int i = 0; i < length; i++) {
            int random = (int) (Math.random() * 10 % 2);
            chromosome = chromosome + random;
        }
        chromosomeObj.chromosome = chromosome;
        chromosomeObj.sumWeight = calcSumWeight(chromosome);
        return chromosomeObj;
    }

    public void generatePool(int length) {
        chromosomes = new Chromosome[population];
        for (int i = 0; i < population; i++) {
            chromosomes[i] = generateChromosome(length);
        }
    }

    public Integer calcSumWeight(String chromosome) {
        // 1이면 집합 S(s), 0이면 집합 S'(sp)
        int scount = 0, spcount = 0;
        int sumWeight = 0;
        ArrayList<Integer> S = new ArrayList<>();
        ArrayList<Integer> SP = new ArrayList<>();
        for (int i = 0; i < chromosome.length(); i++) {
            switch (chromosome.charAt(i)) {
                case '0':
                    spcount++;
                    SP.add(i + 1);
                    break;
                case '1':
                    scount++;
                    S.add(i + 1);
                    break;
            }
        }
        if (scount > spcount) {
            //S' 노드에서 시작해서 S 집합의 노드로 가는 가중치를 합해야함
            ArrayList<Edge> SPStart = new ArrayList<>();
            for (int i : SP) {
                SPStart.addAll(GAGraph.getGraph(i));
            }
            for (Edge edge : SPStart) {
                if (!SP.contains(edge.end)) sumWeight += edge.weight;
            }
        } else {
            ArrayList<Edge> SStart = new ArrayList<>();
            for (int i : S) {
                SStart.addAll(GAGraph.getGraph(i));
            }
            for (Edge edge : SStart) {
                if (!S.contains(edge.end)) sumWeight += edge.weight;
            }
        }
        return sumWeight;
    }

    public Chromosome selectionByTournament() {
        int[] selectedIndexs = new int[6];
        for (int i = 0; i < 6; i++) {
            selectedIndexs[i] = (int) (Math.random() * population);
        }

        Chromosome bestChromosome = null;
        for (int index : selectedIndexs) {
            Chromosome currentChromosome = chromosomes[index];
            if (bestChromosome == null || currentChromosome.sumWeight > bestChromosome.sumWeight) {
                bestChromosome = currentChromosome;
            }
        }

        double probability = Math.random();
        if (probability < 0.7) {
            return bestChromosome;
        } else {
            int rand = (int) (Math.random() * 6);
            return chromosomes[selectedIndexs[rand]];
        }

    }

    public Chromosome uniformCrossOver(Chromosome selected1, Chromosome selected2) {
        char[] newSolution = new char[selected1.chromosome.length()];
        for (int i = 0; i < selected1.chromosome.length(); i++) {
            double point = (float) (Math.random());
            if (point < 0.5) newSolution[i] = selected1.chromosome.charAt(i);
            else newSolution[i] = selected2.chromosome.charAt(i);
        }
        String solution = new String(newSolution);
        int newSumWeight = calcSumWeight(solution);

        Chromosome newChromosome = new Chromosome();
        newChromosome.chromosome = solution;
        newChromosome.sumWeight = newSumWeight;
        return newChromosome;
    }

    public Chromosome mutation(Chromosome childChromosome) {
        // 0.0 ~ 1.0 사이의 랜덤 발생, 돌연변이 발생 확률을 보통 0.015~0.01로 설정
        double point = (float) (Math.random());
        if (point < mutationStrength) {
            int random = (int) (Math.random() * (childChromosome.chromosome.length()));
            char[] charArray = childChromosome.chromosome.toCharArray();
            char changed = (childChromosome.chromosome.charAt(random));
            changed = (changed == '0') ? '1' : '0';
            charArray[random] = changed;
            childChromosome.chromosome = String.valueOf(charArray);
            childChromosome.sumWeight = calcSumWeight(childChromosome.chromosome);
        }
        return childChromosome;
    }

    public void replace(Chromosome[] offspring) {
        // Genitor 스타일

        int k = offspringNumber;
        for (int i = k; i > 0; i--) {
            chromosomes[population - i] = offspring[k - i];
        }
    }

}

class Chromosome implements Comparable<Chromosome> {
    int sumWeight;
    String chromosome;

    @Override
    public int compareTo(Chromosome o) {
        return o.sumWeight - sumWeight;
    }
}

class SortByParanoidQuickSort {
    public static void paranoidQuickSort(Chromosome[] arr, int start, int end) {
        if (start >= end) return;

        int pivotIndex = choosePivot(arr, start, end);

        Chromosome temp = arr[start];
        arr[pivotIndex] = arr[start];
        arr[start] = temp;

        int lo = start + 1;
        int hi = end;
        int pivot = start;

        while (lo <= hi) {
            while (lo <= end && arr[lo].sumWeight >= arr[pivot].sumWeight)
                lo++;
            while (hi > start && arr[hi].sumWeight <= arr[pivot].sumWeight)
                hi--;
            if (lo <= hi) {
                Chromosome temp1 = arr[lo];
                arr[lo] = arr[hi];
                arr[hi] = temp1;
            }
        }

        // swap 부분
        Chromosome temp2 = arr[hi];
        arr[hi] = arr[pivot];
        arr[pivot] = temp2;

        paranoidQuickSort(arr, start, hi - 1);
        paranoidQuickSort(arr, hi + 1, end);

    }

    public static int choosePivot(Chromosome[] arr, int low, int high) {
        int mid = low + (high - low) / 2;
        Chromosome[] candidates = {arr[low], arr[mid], arr[high]};
        Arrays.sort(candidates);
        if (candidates[1] == arr[low]) return low;
        else if (candidates[1] == arr[mid]) return mid;
        else return high;
    }
}
