package util.epub.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import util.epub.domain.Author;
import util.epub.domain.Book;
import util.epub.domain.TOCReference;
import util.epub.epub.EpubWriter;
import util.epub.domain.Resource;

public class Simple1 {
	public static final String DISK_DIR_ROOT = "C:/book1/";
	public static void main(String[] args) {
		try {
			// Create new Book
			Book book = new Book();
	
			// Set the title
			book.getMetadata().addTitle("Epublib test book 1");
			
			// Add an Author
			book.getMetadata().addAuthor(new Author("Joe", "Tester"));
	
			// Set cover image
			book.setCoverImage(new Resource(new FileInputStream(new File(DISK_DIR_ROOT + "0.jpg")), "0.jpg"));
			book.addResource(new Resource(new FileInputStream(new File(DISK_DIR_ROOT + "0.jpg")), "0.jpg"));

			// Add Chapter 1
			TOCReference t1=book.addSection("introduce", new Resource(new FileInputStream(
					new File(DISK_DIR_ROOT + "haha.html")), "haha.html"));
			book.addSection(t1,"11", new Resource(new FileInputStream(
					new File(DISK_DIR_ROOT + "haha.html")), "haha.html"));

			// Add css file
			book.getResources().add(new Resource(new FileInputStream(new File(DISK_DIR_ROOT + "haha.html")), "book1.css"));

	
			// Create EpubWriter
			EpubWriter epubWriter = new EpubWriter();
	
			// Write the Book as Epub
			epubWriter.write(book, new FileOutputStream(DISK_DIR_ROOT +"test1_book1.epub"));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
