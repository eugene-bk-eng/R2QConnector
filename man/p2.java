

import org.apache.commons.collections.map.HashedMap;

import java.util.*;

public class p2 {

    public p2() throws Exception {
        lbeg=System.currentTimeMillis();

        //testGraphColor();

        testWordLadder();
    }

    void testWordLadder() throws Exception {
        log("testWordLadder beg");
        //String beginWord = "hit", endWord = "cog", wordList[] = new String[] {"hot","dot","dog","lot","log","cog"};
        //String beginWord = "hit", endWord = "dog", wordList[] = new String[] {"hot","dot","dog","lot","log","cog"};
        //String beginWord = "hot", endWord = "dog", wordList[] = new String[] {"hot","dog"};
        //String beginWord = "hit", endWord = "cog", wordList[] = new String[] {"hot","dot","dog","lot","log"};
        //String beginWord = "lost", endWord = "miss", wordList[] = new String[] {"most","mist","miss","lost","fist","fish"};
        //String beginWord = "leet", endWord = "code", wordList[] = new String[] {"lest","leet","lose","code","lode","robe","lost"};

        String beginWord = "ab", endWord = "tp", wordList[] = null; int n=45; List<String> list= Lists.newArrayList(endWord); for (int i = 1; i <=n; i++) { String value= Uf.generateRandomCapitalCaseSymbol(beginWord.length(),false); value=value.toLowerCase(); list.add(value);  } wordList=list.toArray(new String[list.size()]);

        ProfNano prf=new ProfNano();
        prf.b();
        Distance dist=ladderLength(beginWord,endWord,Arrays.asList(wordList));
        prf.e();
        log("shortest sequence: " + dist.min + ", # of paths: " + dist.cntPathFound + " elapsed: " + prf.reportTimingMini() );
    }

    /**
     * Given two words (beginWord and endWord), and a dictionary's word list,
     * find the length of shortest transformation sequence from beginWord to endWord, such that:
     * -Only one letter can be changed at a time.
     * -Each transformed word must exist in the word list. Note that beginWord is not a transformed word
     *
     * Return 0 if there is no such transformation sequence.
     * All words have the same length.
     * All words contain only lowercase alphabetic characters.
     * You may assume no duplicates in the word list.
     * You may assume beginWord and endWord are non-empty and are not the same.
     *
     *
     * @param beginWord
     * @param endWord
     * @param wordList
     * @return
     */
    public Distance ladderLength(String beginWord, String endWord, List<String> wordList) {
        // build graph nodes
        ProfNano prf=new ProfNano();
        prf.b();

        Graph g=new Graph();

        // condition
        if( wordList.contains(endWord)==false ) { return new Distance(); }

        // proceed
        Set<String> copy=new LinkedHashSet();
        copy.add(beginWord); copy.add(endWord); copy.addAll(wordList);
        g.allocate( (new HashSet(copy)).size() );
        for (String a: copy) {
            for (String b: copy) {
                if( a.equals("lest") && b.equals("lose")) {
                    int bp=0;
                }
                if( !a.equals(b) && isEdge(a,b)) {
                    g.addEdge( new Edge(a,b) );
                }
            }
        }
        prf.e();
        log("build graph, elapsed: " + prf.reportTimingMini() );

        log("graph has " + g.vertexes.length + " vertexes");
        //print(g);

        prf=new ProfNano();
        prf.b();
        // find beg and end vertex
        Vertex begV=g.getVertex(beginWord);
        Vertex endV=g.getVertex(endWord);
        log("beg: " + begV);
        log("end: " + endV);

        // recursively traverse all possible path from beg to end and keep record of count
        Distance dist=new Distance();
        List<Vertex> path=new ArrayList();
        if( begV!=null && endV!=null ) {
            path.add(begV);
            traverse(g, begV, endV, dist, path);
        }
        prf.e();
        log("traverse graph, elapsed: " + prf.reportTimingMini() );

        return dist;
    }

    void traverse(Graph g, Vertex node, Vertex stop, Distance dist, List<Vertex> path) {
        // stop condition
        if( node.equals(stop) ) {
            //log("Found path: " + path);
            if( path.size()>0 ) {
                dist.cntPathFound++;
                if( dist.min==0 ) { dist.min=path.size(); return; }
                else if( dist.min>path.size() ) { dist.min=path.size(); return; }
            }
        }
        // iterate all children of node.
        for (Vertex n: g.connected(node)) {
            if( !path.contains(n) ) {
                path.add(n);
                traverse(g, n, stop, dist, path);
                if( path.size()>0 ) { path.remove( path.size()-1 ); }
            }
        }
    }

    class Distance {
        long cntPathFound=0;
        int min;
    }

    boolean isEdge(String a, String b) {
        return isOneLetterDifference(a,b);
    }

    // hit and hot... sort them as arrays and compare
    boolean isOneLetterDifference(String a, String b) {
        boolean result=false;
        if( a.length()==b.length() ) {
            byte abyte[]=a.getBytes();
            byte bbyte[]=b.getBytes();
            // reset to 0 found
            for (int i = 0; i < abyte.length; i++) {
                byte tmp=bbyte[i];
                bbyte[i]=abyte[i];
                if( Arrays.equals(abyte,bbyte) ) {
                    return true;
                }else{
                    bbyte[i]=tmp;
                }
            }
        }
        return result;
    }

    void testGraphColor() {
        //Graph g=build("1-2,1-3,2-1,2-3,3-1,3-2");
        Graph g=build("2-1,2-3,2-4,2-5,2-6,1-6,6-5,5-4,4-3");
        print(g);
        color(g);
    }

    void color(Graph g) {
        // preset color
        Set<String> availableColors=new HashSet(Arrays.asList(new String[]{"RED", "BLUE", "GREEN"}));
        //for (int i = 1; i <=g.vertexes.length; i++) { availableColors.add("C" + (i+1));  }
        availableColors=Collections.unmodifiableSet(availableColors);
        Map<Vertex,List<String>> colorChoicesLeft= new HashedMap();
        Map<Vertex,String> usedColors=new HashedMap();
        // start from first
        for (Vertex v: g.vertexes) {
            colorVertex(g, v, usedColors, colorChoicesLeft, availableColors );
        }

        // done, print color
        for (Vertex v: usedColors.keySet()) {
            log("v: " + v.value + ", color: " + usedColors.get(v));
        }
    }

    /** color vertex */
    void colorVertex(Graph g, Vertex current, Map<Vertex,String> usedColors, Map<Vertex,List<String>> colorChoicesLeft, Set<String> availableColors) {

        // is it colored already?
        if( usedColors.containsKey(current) ) { return; }

        // not colored, proceed to color
        List<String> colorChoices;
        if( colorChoicesLeft.containsKey(current) ) {
            colorChoices=colorChoicesLeft.get(current);
        }else {
            colorChoices=new ArrayList(availableColors);
            colorChoicesLeft.put(current,colorChoices);
        }
        // assign color
        Iterator<String> t=colorChoices.iterator();
        while( t.hasNext() ) {
            String color = t.next();
            // validate it against connected
            if( validate(g, current,color, usedColors) ) {
                //
                usedColors.put(current,color);
                // good color
                t.remove();
                // explore its connected vertex and assign them.
                List<Vertex> connections=g.connected(current);
                for (Vertex v: connections) {
                    colorVertex(g,v,usedColors,colorChoicesLeft,availableColors);
                }
            }else {
                // can't be used
                t.remove();
            }
        }
    }

    boolean validate(Graph g, Vertex current, String colorRequested, Map<Vertex,String> usedColors) {
        boolean result=true;
            List<Vertex> connections=g.connected(current);
            for (Vertex v: connections) {
                if( usedColors.containsKey(v) ) {
                    String colorUsed=usedColors.get(v);
                    if( colorUsed.equals(colorRequested) ) {
                        result=false; break;
                    }
                }
            }
        return result;
    }

    /** not directed graph */
    Graph build(String graph) {
        Graph g=new Graph();
            String in[]=graph.split(",");
            List<Edge> edges=new ArrayList(); Set<String> vertices= new HashSet();
            Arrays.stream(in).forEach(s->{String e[]=s.split("-"); edges.add(new Edge(e[0],e[1])); vertices.add(e[0]); vertices.add(e[1]); } );
            g.allocate( vertices.size() );
            for (Edge e: edges) { g.addEdge(e); }
        return g;
    }

    void print(Graph g) {
        for (int i = 0; i < g.cons.length; i++) {
            StringBuilder sb=new StringBuilder();
            for (int j = 0; j < g.cons[i].length; j++) {
                sb.append(g.cons[i][j]); sb.append(" ");
            }
            log(sb.toString());
        }
    }

    class Graph {
        // element to index
        Map<String,Integer> map=new HashMap();
        Map<Integer,String> reverseMap=new HashMap();
        Map<Integer,List<Vertex>> mapConnections=new HashMap();

        Vertex vertexes[];
        int cons[][];
        void allocate(int size) {
            cons=new int[size][size];
            vertexes=new Vertex[size];
        }
        void addEdge(Edge edge) {
            // enter into map
            if( !map.containsKey(edge.a) ) {
                int value=map.size()+1;
                map.put(edge.a, value);
                reverseMap.put(value,edge.a);
            }
            if( !map.containsKey(edge.b) ) {
                int value=map.size()+1;
                map.put(edge.b, value);
                reverseMap.put(value,edge.b);
            }

            cons[map.get(edge.a)-1][map.get(edge.b)-1]=1;
            Vertex vertex_a=new Vertex(edge.a,map.get(edge.a));
            Vertex vertex_b=new Vertex(edge.b,map.get(edge.b));
            vertexes[map.get(edge.a)-1]=vertex_a;
            vertexes[map.get(edge.b)-1]=vertex_b;
            // add to connections
            List<Vertex> list=null;
            if( mapConnections.containsKey(vertex_a.code-1) ) {
                list=mapConnections.get(vertex_a.code-1);
            }else{
                list=new ArrayList();
                mapConnections.put(vertex_a.code-1, list);
            }
            list.add(vertex_b);
        }

        List<Vertex> connected(Vertex v) {
            return mapConnections.get(v.code-1);
        }

        public Vertex getVertex(String value) {
            if( map.containsKey(value)==false ) { return null; }
            return vertexes[map.get(value)-1];
        }
    }

    class Edge{ String a,b;
        public Edge(String a, String b) {
            this.a = a;
            this.b = b;
        }
        public Edge(int a, int b) {
            this.a = Integer.toString(a);
            this.b = Integer.toString(b);
        }
    }

    class Vertex {
        String value;
        int code;
        public Vertex(String value, int code) {
            this.value = value;
            this.code = code;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Vertex)) return false;
            Vertex vertex = (Vertex) o;
            return value == vertex.value;
        }
        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return "{" + value + "[" + code + "]" + '}';
        }
    }

    public static void log(Object s){ System.out.println(s); }
    long lbeg;
}
