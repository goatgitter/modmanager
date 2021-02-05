package com.github.goatgitter.modmanager.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.goatgitter.modmanager.util.Notebook;

public class TestNotebook {
	private String testFileLine1 = "testFile.jar" + System.lineSeparator();
	private String testFileLine2 = "testFile2.jar" + System.lineSeparator();
	private String testFileFullName = "C:\\dev\\TestSpecialCharö\\Desktop\\Games\\Minecraft\\mods\\modmanager\\newModList.txt";
	private Path testPath = null;
	private Path testDir = null;
	
	@BeforeEach
	void setUp() throws Exception {
		createTestFile();
	}

	@AfterEach
	void tearDown() throws Exception {
		deleteTestFile();
		testPath = null;
		testDir = null;
	}
	
	private void createTestFile()
	{
		testPath = Paths.get(testFileFullName);
		testDir = testPath.getParent();
		if (!Files.exists(testDir))
		{
			try {
				Files.createDirectories(testDir);
			} catch (IOException e) {
				e.printStackTrace();
				fail("Unable to create test directory");
			}
		}
		try {
			Files.createFile(testPath);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unable to create test file");
		}
		
		try {
			Files.write(testPath, testFileLine1.getBytes(), StandardOpenOption.APPEND);
			Files.write(testPath, testFileLine2.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unable to write to test file");
		}
	}
	
	private void deleteTestFile()
	{
		try {
			Files.deleteIfExists(testPath);
		} catch (IOException e) {			
			e.printStackTrace();
			fail("Unable to delete test file");
		}
		Path testDirParent = testDir.getParent();
		try {
			FileUtils.deleteDirectory(testDir.toFile());
			FileUtils.deleteDirectory(testDirParent.toFile());
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unable to delete test directories");
		}
	}

	@Test
	void testGet() {
		String result = Notebook.get(testPath);
		assertNotNull(result, "Result should NOT be null");
		assertNotEquals(result, "", "Result should not be empty string");
		assertEquals(result,testFileLine1 + testFileLine2,"Result should equal testFileLine1");
	}
	
	@Test
	void testGetAsList() {
		List<String> result = Notebook.getAsList(testPath);
		List<String> expected = new ArrayList<String>();
		expected.add(testFileLine1.trim());
		expected.add(testFileLine2.trim());
		
		assertNotNull(result, "Result should NOT be null");
		assertNotEquals(result, "", "Result should not be empty string");
		
		assertEquals(result,expected,"Result should equal expected");
	}

}
