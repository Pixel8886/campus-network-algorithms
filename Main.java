import java.util.*;

public class Main {

    static final int N = 6;
    static final int INF = Integer.MAX_VALUE / 2;
    static final char[] NAMES = {'A', 'B', 'C', 'D', 'E', 'F'};

        record Edge(int from, int to, int weight) {
        @Override public String toString() {
            return NAMES[from] + " — " + NAMES[to] + " (" + weight + ")";
        }
    }

    static int id(char c) { return c - 'A'; }

    static Edge e(char a, char b, int w) { return new Edge(id(a), id(b), w); }

    static List<Edge> directedEdges(int deWeight) {
        return new ArrayList<>(List.of(
                e('A', 'B', 4), e('A', 'C', 2),
                e('C', 'B', 1), e('B', 'D', 5),
                e('C', 'D', 8), e('D', 'E', deWeight),
                e('B', 'E', 10), e('E', 'F', 3)));
    }

    static List<Edge> cableEdges() {
        return List.of(
                e('A', 'B', 6), e('A', 'C', 3), e('B', 'C', 2),
                e('B', 'D', 5), e('C', 'D', 4), e('C', 'E', 7),
                e('D', 'E', 2), e('D', 'F', 6), e('E', 'F', 3));
    }

        record PathResult(int[] dist, int[] prev, boolean negativeCycle) {}

        static PathResult dijkstra(List<Edge> edges, int src) {
        List<List<Edge>> adj = new ArrayList<>();
        for (int i = 0; i < N; i++) adj.add(new ArrayList<>());
        for (Edge ed : edges) adj.get(ed.from()).add(ed);

        int[] dist = new int[N];
        int[] prev = new int[N];
        Arrays.fill(dist, INF);
        Arrays.fill(prev, -1);
        dist[src] = 0;

        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        pq.add(new int[]{src, 0});
        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int u = cur[0];
            if (cur[1] > dist[u]) continue;
            for (Edge ed : adj.get(u)) {
                int nd = dist[u] + ed.weight();
                if (nd < dist[ed.to()]) {
                    dist[ed.to()] = nd;
                    prev[ed.to()] = u;
                    pq.add(new int[]{ed.to(), nd});
                }
            }
        }
        return new PathResult(dist, prev, false);
    }

        static PathResult bellmanFord(List<Edge> edges, int src) {
        int[] dist = new int[N];
        int[] prev = new int[N];
        Arrays.fill(dist, INF);
        Arrays.fill(prev, -1);
        dist[src] = 0;

        for (int i = 1; i < N; i++) {
            boolean changed = false;
            for (Edge ed : edges) {
                if (dist[ed.from()] != INF && dist[ed.from()] + ed.weight() < dist[ed.to()]) {
                    dist[ed.to()] = dist[ed.from()] + ed.weight();
                    prev[ed.to()] = ed.from();
                    changed = true;
                }
            }
            if (!changed) break;
        }
        boolean negCycle = false;
        for (Edge ed : edges) {
            if (dist[ed.from()] != INF && dist[ed.from()] + ed.weight() < dist[ed.to()]) {
                negCycle = true;
                break;
            }
        }
        return new PathResult(dist, prev, negCycle);
    }

        static int[][] floydWarshall(List<Edge> edges) {
        int[][] d = new int[N][N];
        for (int[] row : d) Arrays.fill(row, INF);
        for (int i = 0; i < N; i++) d[i][i] = 0;
        for (Edge ed : edges) d[ed.from()][ed.to()] = Math.min(d[ed.from()][ed.to()], ed.weight());

        for (int k = 0; k < N; k++)
            for (int i = 0; i < N; i++)
                for (int j = 0; j < N; j++)
                    if (d[i][k] != INF && d[k][j] != INF && d[i][k] + d[k][j] < d[i][j])
                        d[i][j] = d[i][k] + d[k][j];
        return d;
    }

        static List<Edge> prim(List<Edge> edges, int start) {
        List<List<Edge>> adj = new ArrayList<>();
        for (int i = 0; i < N; i++) adj.add(new ArrayList<>());
        for (Edge ed : edges) {
            adj.get(ed.from()).add(ed);
            adj.get(ed.to()).add(new Edge(ed.to(), ed.from(), ed.weight()));
        }
        boolean[] inTree = new boolean[N];
        List<Edge> result = new ArrayList<>();
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingInt(Edge::weight));

        inTree[start] = true;
        pq.addAll(adj.get(start));
        while (!pq.isEmpty() && result.size() < N - 1) {
            Edge ed = pq.poll();
            if (inTree[ed.to()]) continue;
            inTree[ed.to()] = true;
            result.add(ed);
            for (Edge next : adj.get(ed.to()))
                if (!inTree[next.to()]) pq.add(next);
        }
        return result;
    }

        static List<Edge> kruskal(List<Edge> edges) {
        int[] parent = new int[N];
        for (int i = 0; i < N; i++) parent[i] = i;

        List<Edge> sorted = new ArrayList<>(edges);
        sorted.sort(Comparator.comparingInt(Edge::weight));

        List<Edge> result = new ArrayList<>();
        for (Edge ed : sorted) {
            int a = find(parent, ed.from());
            int b = find(parent, ed.to());
            if (a != b) {
                parent[a] = b;
                result.add(ed);
                if (result.size() == N - 1) break;
            }
        }
        return result;
    }

    static int find(int[] parent, int x) {
        while (parent[x] != x) {
            parent[x] = parent[parent[x]];
            x = parent[x];
        }
        return x;
    }

    static String fmt(int v) { return v >= INF ? "∞" : String.valueOf(v); }

    static String pathString(int[] prev, int target) {
        Deque<Character> stack = new ArrayDeque<>();
        for (int v = target; v != -1; v = prev[v]) stack.push(NAMES[v]);
        StringJoiner sj = new StringJoiner(" → ");
        for (char c : stack) sj.add(String.valueOf(c));
        return sj.toString();
    }

    static void printDistances(int[] dist, String unit) {
        for (int i = 0; i < N; i++)
            System.out.printf("  A → %c : %s %s%n", NAMES[i], fmt(dist[i]), unit);
    }

    static int total(List<Edge> edges) {
        return edges.stream().mapToInt(Edge::weight).sum();
    }

        static boolean connectsAll(List<Edge> edges) {
        int[] parent = new int[N];
        for (int i = 0; i < N; i++) parent[i] = i;
        for (Edge ed : edges) parent[find(parent, ed.from())] = find(parent, ed.to());
        int root = find(parent, 0);
        for (int i = 1; i < N; i++) if (find(parent, i) != root) return false;
        return true;
    }

    static void printTree(String title, List<Edge> tree) {
        System.out.println(title);
        for (Edge ed : tree) System.out.println("  " + ed);
        System.out.println("  Общая стоимость: " + total(tree) + " тыс. руб.");
    }

        static int treeDistance(List<Edge> tree, int from, int to, List<Character> route) {
        List<List<Edge>> adj = new ArrayList<>();
        for (int i = 0; i < N; i++) adj.add(new ArrayList<>());
        for (Edge ed : tree) {
            adj.get(ed.from()).add(ed);
            adj.get(ed.to()).add(new Edge(ed.to(), ed.from(), ed.weight()));
        }
        int[] dist = new int[N];
        int[] prev = new int[N];
        Arrays.fill(dist, -1);
        Arrays.fill(prev, -1);
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(from);
        dist[from] = 0;
        while (!stack.isEmpty()) {
            int u = stack.pop();
            for (Edge ed : adj.get(u))
                if (dist[ed.to()] < 0) {
                    dist[ed.to()] = dist[u] + ed.weight();
                    prev[ed.to()] = u;
                    stack.push(ed.to());
                }
        }
        Deque<Character> st = new ArrayDeque<>();
        for (int v = to; v != -1; v = prev[v]) st.push(NAMES[v]);
        route.addAll(st);
        return dist[to];
    }

    public static void main(String[] args) {
        int A = id('A'), F = id('F');

        System.out.println("=============== ЧАСТЬ 1. МАРШРУТЫ ===============");

        System.out.println("\n[1] Дейкстра (вес = задержка, мс)");
        List<Edge> delays = directedEdges(2);
        PathResult dj = dijkstra(delays, A);
        printDistances(dj.dist(), "мс");
        System.out.println("  Маршрут A → F: " + pathString(dj.prev(), F) + " (" + dj.dist()[F] + " мс)");

        System.out.println("\n[2] Беллман — Форд (вес = стоимость в баллах, D → E = -2)");
        List<Edge> costs = directedEdges(-2);
        PathResult bf = bellmanFord(costs, A);
        System.out.println("  Отрицательный цикл: " + (bf.negativeCycle() ? "да" : "нет"));
        printDistances(bf.dist(), "баллов");
        System.out.println("  Самый дешёвый маршрут A → F: " + pathString(bf.prev(), F)
                + " (" + bf.dist()[F] + " баллов)");

        System.out.println("\n  Добавляем канал E → B с весом -4");
        List<Edge> costs2 = new ArrayList<>(costs);
        costs2.add(e('E', 'B', -4));
        PathResult bf2 = bellmanFord(costs2, A);
        if (bf2.negativeCycle()) {
            System.out.println("  Найден достижимый из A отрицательный цикл: B → D → E → B = 5 + (-2) + (-4) = -1");
            System.out.println("  Конечной минимальной стоимости пути A → F НЕ существует (она стремится к -∞).");
        } else {
            System.out.println("  Минимальная стоимость A → F: " + bf2.dist()[F]);
        }

        System.out.println("\n[3] Флойд — Уоршалл (исходные задержки)");
        int[][] fw = floydWarshall(delays);
        System.out.print("       ");
        for (char c : NAMES) System.out.printf("%4c", c);
        System.out.println();
        for (int i = 0; i < N; i++) {
            System.out.printf("    %c  ", NAMES[i]);
            for (int j = 0; j < N; j++) System.out.printf("%4s", fmt(fw[i][j]));
            System.out.println();
        }
        System.out.println("  Строка A совпадает с результатом Дейкстры: "
                + Arrays.equals(fw[A], dj.dist()));

        System.out.println("\n=============== ЧАСТЬ 2. КАБЕЛЬНАЯ СЕТЬ ===============\n");
        List<Edge> cables = cableEdges();
        List<Edge> primTree = prim(cables, A);
        List<Edge> kruskalTree = kruskal(cables);

        printTree("[4] Прим (старт в A)", primTree);
        System.out.println();
        printTree("[5] Краскал", kruskalTree);

        System.out.println("\nПроверки:");
        for (var entry : new LinkedHashMap<String, List<Edge>>() {{
            put("Прим", primTree);
            put("Краскал", kruskalTree);
        }}.entrySet()) {
            List<Edge> t = entry.getValue();
            System.out.printf("  %-8s соединяет все 6 корпусов: %s; участков = 5: %s%n",
                    entry.getKey(), connectsAll(t), t.size() == N - 1);
        }
        System.out.println("  Стоимости совпадают: " + (total(primTree) == total(kruskalTree))
                + " (" + total(primTree) + " = " + total(kruskalTree) + ")");

        System.out.println("\n=============== ВОПРОС: путь A → B в остове Прима ===============");
        List<Character> route = new ArrayList<>();
        int treeDist = treeDistance(primTree, A, id('B'), route);
        List<Edge> both = new ArrayList<>();
        for (Edge ed : cables) {
            both.add(ed);
            both.add(new Edge(ed.to(), ed.from(), ed.weight()));
        }
        PathResult full = dijkstra(both, A);
        System.out.println("  В дереве Прима:   " + route + ", сумма = " + treeDist);
        System.out.println("  В полном графе:   " + pathString(full.prev(), id('B'))
                + ", сумма = " + full.dist()[id('B')]);
        System.out.println("  Прямой участок A — B стоит 6 > 5.");
        System.out.println("  Путь остаётся кратчайшим: " + (treeDist == full.dist()[id('B')]));
    }
}
