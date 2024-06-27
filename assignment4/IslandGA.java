import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class IslandGA {
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

        int population = 300; // 전체 인구
        int populationPerIsland = 100; // 섬 인구
        int offspringNumber = (int) (population * 1);
        int numIslands = 3; // 섬 개수

        GA NewGA = new GA(population, offspringNumber, GAGraph);

        // <1> 섬 내부 GA 시작
        GA[] islands = new GA[numIslands];
        Chromosome[][] offspringOfIslands = new Chromosome[numIslands][populationPerIsland];
        for(int i = 0; i<numIslands; i++){
            islands[i] = new GA(populationPerIsland, populationPerIsland, GAGraph);
            islands[i].generatePool(nodeCount);
            for(int j = 0; j<70; j++){
                for (int k = 0; k < populationPerIsland; k++) {
                    // 1. Selection : tournament
                    Chromosome[] selected = new Chromosome[2];
                    selected[0] = islands[i].selectionByTournament();
                    selected[1] = islands[i].selectionByTournament();

                    // 2. CrossOver : uniform Crossover
                    Chromosome childChromosome;
                    childChromosome = islands[i].uniformCrossOver(selected[0], selected[1]);

                    // 3. Mutation : nonuniform mutation
                    islands[i].mutation(childChromosome);

                    offspringOfIslands[i][k] = childChromosome;
                }

                // 4. Replace : generation GA
                islands[i].replace(offspringOfIslands[i]);
            }
        }

        // <2> 섬간 교류를 위해 하나의 pool로 합치는 과정
        Chromosome[] tempChromosome = new Chromosome[offspringNumber];
        for(int i = 0; i<numIslands; i++){
            System.arraycopy(islands[i].chromosomes, 0, tempChromosome, i * 100, 100);
        }

        // <3> 섬간 교류 완료 후 새로운 GA 클래스 초기화
        NewGA.chromosomes = (tempChromosome);
        Chromosome[] offspring = new Chromosome[offspringNumber];
        int j = 0;

        // <4> 최종 섬에서의 GA 시작
        while (true) {
            for (int i = 0; i < offspringNumber; i++) {
                // 1. Selection : tournament
                Chromosome[] selected = new Chromosome[2];
                selected[0] = NewGA.selectionByTournament();
                selected[1] = NewGA.selectionByTournament();

                // 2. CrossOver : uniform Crossover
                Chromosome childChromosome;
                childChromosome = NewGA.uniformCrossOver(selected[0], selected[1]);

                // 3. Mutation : nonuniform mutation
                NewGA.mutation(childChromosome);

                offspring[i] = childChromosome;

            }

            // 4. Replace : generation GA
            NewGA.replace(offspring);

            if(j >= 50) break;
            j += 1;

        }

        SortByParanoidQuickSort.paranoidQuickSort(NewGA.chromosomes, 0, NewGA.chromosomes.length - 1);
        Chromosome solution = NewGA.chromosomes[0];
        LocalOptimization.localOpt(solution);
        Map<Integer, Chromosome> finalChormosomes = new HashMap<>();
        for(Chromosome i : LocalOptimization.candidates){
            if(finalChormosomes.containsKey(LocalOptimization.calcDist(solution,i))){
                if(finalChormosomes.get(LocalOptimization.calcDist(solution,i)).sumWeight < i.sumWeight){
                    finalChormosomes.put(LocalOptimization.calcDist(solution,i), i);
                }
            }
            else{
                finalChormosomes.put(LocalOptimization.calcDist(solution,i), i);
            }
        }

        Chromosome bestResult = new Chromosome();
        for (Integer key: finalChormosomes.keySet()){
            bestResult = finalChormosomes.get(key);
        }

//        System.out.println(bestResult.sumWeight);
        for (int i = 0; i < bestResult.chromosome.length(); i++) {
            if (bestResult.chromosome.charAt(i) == '1') fw.write(i + 1 + " ");
            else continue;
        }
        long afterTime = System.currentTimeMillis();
//        System.out.println((afterTime - beforeTime)/1000);
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
    static Graph GAGraph;

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

    public static Integer calcSumWeight(String chromosome) {
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

    public static GA[] migrate(GA[] islands){
        Random random = new Random();
        // 20번 정도 랜덤으로 섬간 교류가 발생
        for(int i = 0; i < 20; i++){
            int migrateFrom = random.nextInt(islands.length);
            int migrateTo = ((migrateFrom + 1 + random.nextInt(islands.length - 1)) % islands.length);

            int index= random.nextInt(islands[migrateFrom].chromosomes.length);
            islands[migrateTo].chromosomes[index] = islands[migrateFrom].chromosomes[index];
        }
        return islands;
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

class LocalOptimization {
    static ArrayList<Chromosome> candidates = new ArrayList<>();
    public static Chromosome changeIndex(int index, Chromosome solution){
        Chromosome tempChromosome = new Chromosome();
        tempChromosome.chromosome = solution.chromosome;

        char[] tempArray = tempChromosome.chromosome.toCharArray();

        char changed = (solution.chromosome.charAt(index));
        changed = (changed == '0') ? '1' : '0';
        tempArray[index] = changed;
        tempChromosome.chromosome = String.valueOf(tempArray);
        tempChromosome.sumWeight = GA.calcSumWeight(tempChromosome.chromosome);
        return tempChromosome;
    }
    public static int calcDelta(int index, Chromosome solution){
        Chromosome tempChromosome = changeIndex(index,solution);
        return (tempChromosome.sumWeight - solution.sumWeight);
    };

    public static void localOpt(Chromosome solution){
        candidates = new ArrayList<>();
        int V = solution.chromosome.length();
        List<Integer> permutation = generateRandomPermutation(V);
        boolean improved = true;

        while(improved){
            improved = false;

            for(int i = 0; i<V; i++){
                int index = permutation.get(i);
                if(calcDelta(index, solution) > 0){
                    solution = changeIndex(index,solution);
                    candidates.add(solution);
                    improved = true;
                }
            }
        }
    }

    public static List<Integer> generateRandomPermutation(int V){
        List<Integer> permutation = new ArrayList<>();
        for(int i = 0; i < V; i++){
            permutation.add(i);
        }
        Collections.shuffle(permutation);
        return permutation;
    }

    public static int calcDist(Chromosome solution, Chromosome candidate){
        int dist = 0;
        for(int i=0; i<solution.chromosome.length(); i++) {
            if ((solution.chromosome.charAt(i) ^ candidate.chromosome.charAt(i)) == 1) dist++;
        }

        return dist;
    }
}