package org.random.search;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import static javax.swing.UIManager.put;

public class Indexer {

    private IndexWriter writer;

    public Indexer(String indexDir) throws IOException {
        Directory dir = FSDirectory.open(new File(indexDir).toPath());
        IndexWriterConfig config = new IndexWriterConfig();
        writer = new IndexWriter(dir, config);

    }

    public int index(String dataDir, FileFilter filter) throws Exception {
        File[] files = new File(dataDir).listFiles();
        for (File f : files) {
            if (!f.isDirectory() && !f.isHidden() && f.exists() && f.canRead() && (filter == null || filter.accept(f))) {
                indexFile(f);
            }
        }
        return writer.numRamDocs();
    }

    private static class TextFilesFilter implements FileFilter {
        public boolean accept(File file) {
            return file.getName().toLowerCase().endsWith(".txt") || file.getName().toLowerCase().endsWith(".java");
        }
    }

    protected Document getDocument(File f) throws Exception {
        Document doc = new Document();
        doc.add(new TextField("contents", new FileReader(f)));
        doc.add(new TextField("filename", f.getName(), Field.Store.YES));
        doc.add(new TextField("fullpath", f.getCanonicalPath(), Field.Store.YES));
        return doc;
    }

    private void indexFile(File f) throws Exception {
        System.out.println("Indexing file " + f.getAbsolutePath());
        Document document = getDocument(f);
        writer.addDocument(document);
    }


    public void close() throws IOException {
        writer.close();
    }

    public void search(String indexDir, String query) throws IOException, ParseException {
        Directory dir = FSDirectory.open(new File(indexDir).toPath());
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher indexSearcher = new IndexSearcher(reader);
        Query q = new QueryParser("contents", new StandardAnalyzer()).parse(query);
        TopDocs hits = indexSearcher.search(q, 10);
        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = indexSearcher.doc(scoreDoc.doc);
            System.out.println(doc.get("fullpath"));
        }
        dir.close();
    }

    public static void main(String[] args) throws Exception {


        long start = System.currentTimeMillis();
        Indexer indexer = new Indexer("/Users/minukolunu/Projects/Random/experiments");
        /*int numIndexed;
        try {
            numIndexed = indexer.index("/Users/minukolunu/Projects", new TextFilesFilter());
        } finally {
            indexer.close();
        }
        long end = System.currentTimeMillis();
        System.out.println("Indexing " + numIndexed + " files took "
                + (end - start) + " milliseconds");*/


        indexer.search("/Users/minukolunu/Projects/Random/experiments", "list");
    }
}
