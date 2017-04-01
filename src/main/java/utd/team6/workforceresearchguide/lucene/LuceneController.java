package utd.team6.workforceresearchguide.lucene;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.exception.TikaException;
import utd.team6.workforceresearchguide.main.Utils;

//@author Michael Haertling
// If possible it is best to reulse the IndexWriter as much as possible
public class LuceneController {

    private static int NUM_SEARCH_THREADS = 5;

    FSDirectory dir;
    StandardAnalyzer analyzer;

    IndexWriter writer;

    DirectoryReader reader;

    private LuceneController() {
        analyzer = new StandardAnalyzer();
    }

    public LuceneController(String lucenePath) throws IOException {
        this();
        //Let Lucene determine what type of FSDirectory to use for the machine
        dir = FSDirectory.open(FileSystems.getDefault().getPath(lucenePath));
    }

    public static void main(String[] args) throws IOException, TikaException, IndexingSessionNotStartedException, ReadSessionNotStartedException {
        LuceneController cont = new LuceneController("_lucene_files_");
        String filePaths[] = {
            "C:\\Users\\Michael\\Google Drive\\School\\UTD Year 4\\Semester 2\\CV Readings\\Attached at the Hip.docx",
            "C:\\Users\\Michael\\Google Drive\\School\\UTD Year 4\\Semester 2\\CV Readings\\LifeDegredationPlan.docx",
            "C:\\Users\\Michael\\Google Drive\\School\\UTD Year 4\\Semester 2\\CV Readings\\ItsComplicated.pdf"};
        //System.out.println(DocumentReader.readDocument(filePaths[0]));

//        cont.startIndexingSession();
//        for (String fpath : filePaths) {
//            cont.indexNewDocument(fpath);
//        }
//        cont.stopIndexingSession();
//
//        cont.startReadSession();
//        cont.startIndexingSession();
//        
//        cont.tagDocument(filePaths[0], "tag1");
//        cont.tagDocument(filePaths[0], "tag2");
//        cont.tagDocument(filePaths[1], "tag1");
//        cont.tagDocument(filePaths[2], "tag2");
//        
//        cont.stopReadSession();
//        cont.stopIndexingSession();
//        cont.startIndexingSession();
//        cont.deleteDocument(filePaths[0]);
//        cont.stopIndexingSession();
//        cont.startReadSession();
//        cont.basicSearchTest("tag2");
//        cont.stopReadSession();
        long time = System.currentTimeMillis();
        String files = "C:\\Users\\Michael\\Downloads\\TESTDOCS\\TESTDOCS";
        ArrayList<String> paths = Utils.extractAllPaths(files);

        cont.startIndexingSession();
        for (int i = 0; i < paths.size(); i++) {
            long time2 = System.currentTimeMillis();
            try {
                cont.indexNewDocument(paths.get(i));
            } catch (TikaException ex) {
                System.err.println(ex);
            }
//            System.out.println("DocNum: "+i+"/"+paths.size()+"\tTime: "+(System.currentTimeMillis()-time2));
        }
        long time2 = System.currentTimeMillis();
        cont.stopIndexingSession();
        System.out.println("END SESSION: " + (System.currentTimeMillis() - time2));
        System.out.println(System.currentTimeMillis() - time);
    }

    /**
     * Start a new indexing session. This must be done before any Lucene
     * indexing can take place.
     *
     * @throws IOException
     */
    public void startIndexingSession() throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        writer = new IndexWriter(dir, config);
    }

    /**
     * Ends a previously started indexing session. If no session is currently
     * active, this function will do nothing.
     *
     * @throws IOException
     */
    public void stopIndexingSession() throws IOException {
        if (writer != null) {
            writer.commit();
            writer.close();
            writer = null;
        }
    }

    /**
     * Starts a new reading session. A reading session must be active for
     * certain functions to work.
     *
     * @throws IOException
     */
    public void startReadSession() throws IOException {
        reader = DirectoryReader.open(dir);
    }

    /**
     * Ends a previously started reading session. If no session is currently
     * active, this function will do nothing.
     *
     * @throws IOException
     */
    public void stopReadSession() throws IOException {
        if (readSessionActive()) {
            reader.close();
            reader = null;
        }
    }

    /**
     * Ends a previously started indexing session and reverts any changes that
     * occurred within that session.
     *
     * @throws IOException
     */
    public void rollbackIndexingSession() throws IOException {
        if (indexingSessionActive()) {
            writer.rollback();
            writer = null;
        }
    }

    /**
     * Returns true if a read session is currently active.
     *
     * @return
     */
    public boolean readSessionActive() {
        return reader != null;
    }

    /**
     * Returns true if an indexing session is currently active.
     *
     * @return
     */
    public boolean indexingSessionActive() {
        return writer != null;
    }

    /**
     * Indexes a new document and adds it to the Lucene directory. An indexing
     * session must be active when this function is called.
     *
     * @param path
     * @throws IOException
     * @throws TikaException
     * @throws IndexingSessionNotStartedException
     */
    public void indexNewDocument(String path) throws IOException, TikaException, IndexingSessionNotStartedException {

        if (!indexingSessionActive()) {
            throw new IndexingSessionNotStartedException();
        }

        Document doc = new Document();

        //Add the document path
        doc.add(new StringField("path", path, Store.YES));

        //Add the document name
        doc.add(new TextField("title", new File(path).getName(), Store.YES));

        //Scan the document text
        String text = Utils.readDocument(path);
        if (!text.isEmpty()) {
            doc.add(new TextField("content", text, Store.YES));
        }

        try {
            writer.addDocument(doc);
        } catch (NullPointerException e) {
            throw new IndexingSessionNotStartedException();
        }
    }

    /**
     * Indexes multiple documents and adds them to the Lucene directory. An
     * indexing session must be active when this function is called.
     *
     * @param paths
     * @throws IOException
     * @throws TikaException
     * @throws IndexingSessionNotStartedException
     */
    public void indexNewDocuments(String[] paths) throws IOException, TikaException, IndexingSessionNotStartedException {
        for (String path : paths) {
            indexNewDocument(path);
        }
    }

    /**
     * Removes a document from the Lucene directory. An indexing session must be
     * active when this function is called.
     *
     * @param path
     * @throws IOException
     * @throws
     * utd.team6.workforceresearchguide.lucene.IndexingSessionNotStartedException
     */
    public void deleteDocument(String path) throws IOException, IndexingSessionNotStartedException {
        if (!indexingSessionActive()) {
            throw new IndexingSessionNotStartedException();
        }
        writer.deleteDocuments(new Term("path", path));
    }

    /**
     * Get the Document object the corresponds to the specified document path. A
     * read session must be active.
     *
     * @param docPath
     * @return
     * @throws IOException
     */
    private Document getDocument(String docPath) throws IOException, ReadSessionNotStartedException {
        if (!readSessionActive()) {
            throw new ReadSessionNotStartedException();
        }
        IndexSearcher search = new IndexSearcher(reader);
        Query query = new TermQuery(new Term("path", docPath));
        TopDocs docs = search.search(query, 1);
        return reader.document(docs.scoreDocs[0].doc);
    }

    /**
     * Appends a new tag field to the specified document. Both a read and
     * indexing session must be active.
     *
     * @param docPath
     * @param tag
     * @throws java.io.IOException
     * @throws
     * utd.team6.workforceresearchguide.lucene.IndexingSessionNotStartedException
     * @throws
     * utd.team6.workforceresearchguide.lucene.ReadSessionNotStartedException
     */
    public void tagDocument(String docPath, String tag) throws IOException, IndexingSessionNotStartedException, ReadSessionNotStartedException {
        if (!indexingSessionActive()) {
            throw new IndexingSessionNotStartedException();
        }
        Document doc = getDocument(docPath);
        doc.add(new TextField("tag", tag, Store.YES));
        writer.updateDocument(new Term("path", docPath), doc);
    }

    /**
     * Conducts a search based off a String query. This search function uses
     * BooleanQueries created from the whitespace separated terms within the
     * provided query.
     *
     * @param query
     * @param numTopScores
     * @throws IOException
     * @throws
     * utd.team6.workforceresearchguide.lucene.ReadSessionNotStartedException
     */
    public void search(String query, int numTopScores) throws IOException, ReadSessionNotStartedException {
        //TermQuery - Matches a single term (can be combined with BooleanQuery)
        //BooleanQuery
        //WildcardQuery
        //PhraseQuery - Matches a particular sequence of terms
        //PrefixQuery
        //MultiPhraseQuery

        //LeafReader.terms(String)
        //Build the query into multiple phases
        //Phase 1: tags, titles, dates
        //Phase 2: content
        if (!readSessionActive()) {
            throw new ReadSessionNotStartedException();
        }

        BooleanQuery.Builder phase1QueryBuilder = new BooleanQuery.Builder();
        BooleanQuery.Builder phase2QueryBuilder = new BooleanQuery.Builder();
        String[] terms = query.split(" ");
        for (String term : terms) {
            phase1QueryBuilder.add(new BooleanClause(new TermQuery(new Term("tag", term)), BooleanClause.Occur.SHOULD));
            phase1QueryBuilder.add(new BooleanClause(new TermQuery(new Term("title", term)), BooleanClause.Occur.SHOULD));
            phase2QueryBuilder.add(new BooleanClause(new TermQuery(new Term("content", term)), BooleanClause.Occur.SHOULD));
        }

        BooleanQuery phase1Query = phase1QueryBuilder.build();
        BooleanQuery phase2Query = phase2QueryBuilder.build();

        LuceneSearchSession search = new LuceneSearchSession(reader, NUM_SEARCH_THREADS, numTopScores, phase1Query, phase2Query);

        //SimpleCollector
        //TopDocsCollector
    }

    private void basicSearchTest(String query) throws IOException, ReadSessionNotStartedException {
        if (!readSessionActive()) {
            throw new ReadSessionNotStartedException();
        }
        BooleanQuery.Builder phase1QueryBuilder = new BooleanQuery.Builder();
        BooleanQuery.Builder phase2QueryBuilder = new BooleanQuery.Builder();
        String[] terms = query.split(" ");
        for (String term : terms) {
            System.out.println("Adding Term to Query: " + term);
            phase1QueryBuilder.add(new BooleanClause(new TermQuery(new Term("tag", term)), BooleanClause.Occur.SHOULD));
            phase1QueryBuilder.add(new BooleanClause(new TermQuery(new Term("title", term)), BooleanClause.Occur.SHOULD));
            phase2QueryBuilder.add(new BooleanClause(new TermQuery(new Term("content", term)), BooleanClause.Occur.SHOULD));
        }

        BooleanQuery phase1Query = phase1QueryBuilder.build();
        BooleanQuery phase2Query = phase2QueryBuilder.build();

        IndexSearcher search = new IndexSearcher(reader);

        System.out.println("Beginning phase 1 search");
        TopDocs td = search.search(phase1Query, 10);
        ScoreDoc[] sds = td.scoreDocs;
        for (ScoreDoc sd : sds) {
            System.out.println(sd.doc + "\t" + reader.document(sd.doc).get("title") + "\t" + sd.score);
        }
        if (td.totalHits == 0) {
            System.out.println("No matches found...");
        }

        System.out.println("\nBeginning phase 2 search");
        td = search.search(phase2Query, 10);
        sds = td.scoreDocs;
        for (ScoreDoc sd : sds) {
            System.out.println(sd.doc + "\t" + reader.document(sd.doc).get("title") + "\t" + sd.score);
        }
        if (td.totalHits == 0) {
            System.out.println("No matches found...");
        }

        reader.close();
    }

}