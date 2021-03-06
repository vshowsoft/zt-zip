package org.zeroturnaround.zip;
/**
 *    Copyright (C) 2012 ZeroTurnaround LLC <support@zeroturnaround.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.zeroturnaround.zip.commons.FileUtils;
import org.zeroturnaround.zip.commons.IOUtils;

import junit.framework.TestCase;


/** @noinspection ResultOfMethodCallIgnored*/
public class ZipUtilTest extends TestCase {

  /** @noinspection ConstantConditions*/
  private File file(String name) {
    return new File(getClass().getClassLoader().getResource(name).getPath());
  }

  public void testPackEntryStream() {
    File src = file("TestFile.txt");
    byte[] bytes = ZipUtil.packEntry(src);
    boolean processed = ZipUtil.handle(new ByteArrayInputStream(bytes), "TestFile.txt", new ZipEntryCallback() {

      public void process(InputStream in, ZipEntry zipEntry) throws IOException {
      }
    });
    assertTrue(processed);
  }

  public void testPackEntryFile() throws Exception {
    File fileToPack = file("TestFile.txt");
    File dest = File.createTempFile("temp", null);
    ZipUtil.packEntry(fileToPack, dest);
    assertTrue(dest.exists());

    ZipUtil.explode(dest);
    assertTrue((new File(dest, "TestFile.txt")).exists());
    // if fails then maybe somebody changed the file contents and did not update
    // the test
    assertEquals(108, (new File(dest, "TestFile.txt")).length());
  }

  public void testPackEntryFileWithNameParameter() throws Exception {
    File fileToPack = file("TestFile.txt");
    File dest = File.createTempFile("temp", null);
    ZipUtil.packEntry(fileToPack, dest, "TestFile-II.txt");
    assertTrue(dest.exists());

    ZipUtil.explode(dest);
    assertTrue((new File(dest, "TestFile-II.txt")).exists());
    // if fails then maybe somebody changed the file contents and did not update
    // the test
    assertEquals(108, (new File(dest, "TestFile-II.txt")).length());
  }

  public void testPackEntryFileWithNameMapper() throws Exception {
    File fileToPack = file("TestFile.txt");
    File dest = File.createTempFile("temp", null);
    ZipUtil.packEntry(fileToPack, dest, new NameMapper() {
      public String map(String name) {
        return "TestFile-II.txt";
      }
    });
    assertTrue(dest.exists());

    ZipUtil.explode(dest);
    assertTrue((new File(dest, "TestFile-II.txt")).exists());
    // if fails then maybe somebody changed the file contents and did not update
    // the test
    assertEquals(108, (new File(dest, "TestFile-II.txt")).length());
  }

  public void testUnpackEntryFromFile() throws IOException {
    final String name = "foo";
    final byte[] contents = "bar".getBytes();


    File file = File.createTempFile("temp", null);
    try {
      // Create the ZIP file
      ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));
      try {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(contents);
        zos.closeEntry();
      }
      finally {
        IOUtils.closeQuietly(zos);
      }

      // Test the ZipUtil
      byte[] actual = ZipUtil.unpackEntry(file, name);
      assertNotNull(actual);
      assertEquals(new String(contents), new String(actual));
    }
    finally {
      FileUtils.deleteQuietly(file);
    }
  }

  public void testUnpackEntryFromStreamToFile() throws IOException {
    final String name = "foo";
    final byte[] contents = "bar".getBytes();

    File file = File.createTempFile("temp", null);
    try {
      // Create the ZIP file
      ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));
      try {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(contents);
        zos.closeEntry();
      }
      finally {
        IOUtils.closeQuietly(zos);
      }

      FileInputStream fis = new FileInputStream(file);

      File outputFile = File.createTempFile("temp-output", null);

      boolean result = ZipUtil.unpackEntry(fis, name, outputFile);
      assertTrue(result);

      BufferedInputStream bis = new BufferedInputStream(new FileInputStream(outputFile));
      byte[] actual = new byte[1024];
      int read = bis.read(actual);
      bis.close();

      assertEquals(new String(contents), new String(actual, 0, read));
    }
    finally {
      FileUtils.deleteQuietly(file);
    }
  }

  public void testUnpackEntryFromStream() throws IOException {
    final String name = "foo";
    final byte[] contents = "bar".getBytes();

    File file = File.createTempFile("temp", null);
    try {
      // Create the ZIP file
      ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));
      try {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(contents);
        zos.closeEntry();
      }
      finally {
        IOUtils.closeQuietly(zos);
      }

      FileInputStream fis = new FileInputStream(file);
      // Test the ZipUtil
      byte[] actual = ZipUtil.unpackEntry(fis, name);
      assertNotNull(actual);
      assertEquals(new String(contents), new String(actual));
    }
    finally {
      FileUtils.deleteQuietly(file);
    }
  }

  public void testDuplicateEntryAtAdd() throws IOException {
    File src = file("duplicate.zip");

    File dest = File.createTempFile("temp", null);
    try {
      ZipUtil.addEntries(src, new ZipEntrySource[0], dest);
    }
    finally {
      FileUtils.deleteQuietly(dest);
    }
  }

  public void testDuplicateEntryAtReplace() throws IOException {
    File src = file("duplicate.zip");

    File dest = File.createTempFile("temp", null);
    try {
      ZipUtil.replaceEntries(src, new ZipEntrySource[0], dest);
    }
    finally {
      FileUtils.deleteQuietly(dest);
    }
  }

  public void testDuplicateEntryAtAddOrReplace() throws IOException {
    File src = file("duplicate.zip");

    File dest = File.createTempFile("temp", null);
    try {
      ZipUtil.addOrReplaceEntries(src, new ZipEntrySource[0], dest);
    }
    finally {
      FileUtils.deleteQuietly(dest);
    }
  }

  public void testUnexplode() throws IOException {
    File file = File.createTempFile("tempFile", null);
    File tmpDir = file.getParentFile();

    unexplodeWithException(file, "shouldn't be able to unexplode file that is not a directory");
    assertTrue("Should be able to delete tmp file", file.delete());
    unexplodeWithException(file, "shouldn't be able to unexplode file that doesn't exist");

    // create empty tmp dir with the same name as deleted file
    File dir = new File(tmpDir, file.getName());
    dir.deleteOnExit();
    assertTrue("Should be able to create directory with the same name as there was tmp file", dir.mkdir());

    unexplodeWithException(dir, "shouldn't be able to unexplode dir that doesn't contain any files");

    // unexplode should succeed with at least one file in directory
    File.createTempFile("temp", null, dir);
    ZipUtil.unexplode(dir);

    assertTrue("zip file should exist with the same name as the directory that was unexploded", dir.exists());
    assertTrue("unexploding input directory should have produced zip file with the same name", !dir.isDirectory());
    assertTrue("Should be able to delete zip that was created from directory", dir.delete());
  }

  public void testPackEntries() throws Exception {
    File fileToPack = file("TestFile.txt");
    File fileToPackII = file("TestFile-II.txt");
    File dest = File.createTempFile("temp", null);
    ZipUtil.packEntries(new File[]{fileToPack, fileToPackII}, dest);
    assertTrue(dest.exists());

    ZipUtil.explode(dest);
    assertTrue((new File(dest, "TestFile.txt")).exists());
    assertTrue((new File(dest, "TestFile-II.txt")).exists());
    // if fails then maybe somebody changed the file contents and did not update the test
    assertEquals(108, (new File(dest, "TestFile.txt")).length());
    assertEquals(103, (new File(dest, "TestFile-II.txt")).length());
  }

  public void testPackEntriesWithNameMapper() throws Exception {
    File fileToPack = file("TestFile.txt");
    File fileToPackII = file("TestFile-II.txt");
    File dest = File.createTempFile("temp", null);
    ZipUtil.packEntries(new File[] { fileToPack, fileToPackII }, dest, new NameMapper() {
      public String map(String name) {
        return "Changed-" + name;
      }
    });
    assertTrue(dest.exists());

    ZipUtil.explode(dest);
    assertTrue((new File(dest, "Changed-TestFile.txt")).exists());
    assertTrue((new File(dest, "Changed-TestFile-II.txt")).exists());
    // if fails then maybe somebody changed the file contents and did not update
    // the test
    assertEquals(108, (new File(dest, "Changed-TestFile.txt")).length());
    assertEquals(103, (new File(dest, "Changed-TestFile-II.txt")).length());
  }

  public void testZipException() {
    boolean exceptionThrown = false;
    File target = new File("weeheha");
    try {
      ZipUtil.pack(new File("nonExistent"), target);
    }
    catch (ZipException e) {
      exceptionThrown = true;
    }
    assertFalse("Target file is created when source does not exist", target.exists());
    assertTrue(exceptionThrown);
  }

  public void testPackEntriesWithNamesList() throws Exception {
    File fileToPack = file("TestFile.txt");
    File fileToPackII = file("TestFile-II.txt");
    File dest = File.createTempFile("temp", null);

    ZipUtil.pack(
      FileSource.pair(
          new File[]{fileToPack, fileToPackII},
          new String[]{"Changed-TestFile.txt", "Changed-TestFile-II.txt"}),
      dest
    );

    assertTrue(dest.exists());

    ZipUtil.explode(dest);
    assertTrue((new File(dest, "Changed-TestFile.txt")).exists());
    assertTrue((new File(dest, "Changed-TestFile-II.txt")).exists());
    // if fails then maybe somebody changed the file contents and did not update
    // the test
    assertEquals(108, (new File(dest, "Changed-TestFile.txt")).length());
    assertEquals(103, (new File(dest, "Changed-TestFile-II.txt")).length());
  }

  public void testPreserveRoot() throws Exception {
    File dest = File.createTempFile("temp", null);
    File parent = file("TestFile.txt").getParentFile();
    ZipUtil.pack(parent, dest, true);
    ZipUtil.explode(dest);
    assertTrue((new File(dest, parent.getName())).exists());
  }

  private void unexplodeWithException(File file, String message) {
    try {
      ZipUtil.unexplode(file);
    }
    catch (Exception e) {
      return;
    }
    fail(message);
  }

  public void testArchiveEquals() {
    File src = file("demo.zip");
    // byte-by-byte copy
    File src2 = file("demo-copy.zip");
    assertTrue(ZipUtil.archiveEquals(src, src2));

    // entry by entry copy
    File src3 = file("demo-copy-II.zip");
    assertTrue(ZipUtil.archiveEquals(src, src3));
  }

  public void testRepackArchive() throws IOException {
    File src = file("demo.zip");
    File dest = File.createTempFile("temp", null);
    ZipUtil.repack(src, dest, 1);
    assertTrue(ZipUtil.archiveEquals(src, dest));
  }

  public void testContainsAnyEntry() throws IOException {
    File src = file("demo.zip");
    boolean exists = ZipUtil.containsAnyEntry(src, new String[] { "foo.txt", "bar.txt" });
    assertTrue(exists);

    exists = ZipUtil.containsAnyEntry(src, new String[] { "foo.txt", "does-not-exist.txt" });
    assertTrue(exists);

    exists = ZipUtil.containsAnyEntry(src, new String[] { "does-not-exist-I.txt", "does-not-exist-II.txt" });
    assertFalse(exists);
  }

  public void testAddEntry() throws IOException {
    File initialSrc = file("demo.zip");

    File src = File.createTempFile("ztr", ".zip");
    FileUtils.copyFile(initialSrc, src);

    final String fileName = "TestFile.txt";
    if(ZipUtil.containsEntry(src, fileName)) {
      ZipUtil.removeEntry(src, fileName);
    }
    assertFalse(ZipUtil.containsEntry(src, fileName));
    File newEntry = file(fileName);
    File dest = File.createTempFile("temp.zip", null);

    ZipUtil.addEntry(src, fileName, newEntry, dest);
    assertTrue(ZipUtil.containsEntry(dest, fileName));
    FileUtils.forceDelete(src);
  }
  
  public void testKeepEntriesState() throws IOException {
    File src = file("demo-keep-entries-state.zip");
    final String existingEntryName = "TestFile.txt";
    final String fileNameToAdd = "TestFile-II.txt";
    assertFalse(ZipUtil.containsEntry(src, fileNameToAdd));
    File newEntry = file(fileNameToAdd);
    File dest = File.createTempFile("temp.zip", null);
    ZipUtil.addEntry(src, fileNameToAdd, newEntry, dest);
    
    ZipEntry srcEntry = new ZipFile(src).getEntry(existingEntryName);
    ZipEntry destEntry = new ZipFile(dest).getEntry(existingEntryName);
    assertTrue(srcEntry.getCompressedSize() == destEntry.getCompressedSize());
  }

  public void testRemoveEntry() throws IOException {
    File src = file("demo.zip");

    File dest = File.createTempFile("temp", null);
    try {
      ZipUtil.removeEntry(src, "bar.txt", dest);
      assertTrue("Result zip misses entry 'foo.txt'", ZipUtil.containsEntry(dest, "foo.txt"));
      assertTrue("Result zip misses entry 'foo1.txt'", ZipUtil.containsEntry(dest, "foo1.txt"));
      assertTrue("Result zip misses entry 'foo2.txt'", ZipUtil.containsEntry(dest, "foo2.txt"));
      assertFalse("Result zip still contains 'bar.txt'", ZipUtil.containsEntry(dest, "bar.txt"));
    }
    finally {
      FileUtils.deleteQuietly(dest);
    }
  }

  public void testRemoveMissingEntry() throws IOException {
    File src = file("demo.zip");
    assertFalse("Source zip contains entry 'missing.txt'", ZipUtil.containsEntry(src, "missing.txt"));

    File dest = File.createTempFile("temp", null);
    try {
      ZipUtil.removeEntry(src, "missing.txt", dest);
    }
    finally {
      FileUtils.deleteQuietly(dest);
    }
  }

  public void testRemoveDirs() throws IOException {
    File src = file("demo-dirs.zip");

    File dest = File.createTempFile("temp", null);
    try {
      ZipUtil.removeEntries(src, new String[] { "bar.txt", "a/b" }, dest);

      assertFalse("Result zip still contains 'bar.txt'", ZipUtil.containsEntry(dest, "bar.txt"));
      assertFalse("Result zip still contains dir 'a/b'", ZipUtil.containsEntry(dest, "a/b"));
      assertTrue("Result doesn't contain 'attic'", ZipUtil.containsEntry(dest, "attic/treasure.txt"));
      assertTrue("Entry whose prefix is dir name is removed too: 'b.txt'", ZipUtil.containsEntry(dest, "a/b.txt"));
      assertFalse("Entry in a removed dir is still there: 'a/b/c.txt'", ZipUtil.containsEntry(dest, "a/b/c.txt"));

    }
    finally {
      FileUtils.deleteQuietly(dest);
    }
  }

  public void testHandle() {
    File src = file("demo.zip");

    boolean entryFound = ZipUtil.handle(src, "foo.txt", new ZipEntryCallback() {
      public void process(InputStream in, ZipEntry zipEntry) throws IOException {
        assertEquals("foo.txt", zipEntry.getName());
      }
    });
    assertTrue(entryFound);

    entryFound = ZipUtil.handle(src, "non-existent-file.txt", new ZipEntryCallback() {
      public void process(InputStream in, ZipEntry zipEntry) throws IOException {
        throw new RuntimeException("This should not happen!");
      }
    });
    assertFalse(entryFound);
  }

  public void testIterate() {
    File src = file("demo.zip");
    final Set files = new HashSet();
    files.add("foo.txt");
    files.add("bar.txt");
    files.add("foo1.txt");
    files.add("foo2.txt");

    ZipUtil.iterate(src, new ZipInfoCallback() {

      public void process(ZipEntry zipEntry) throws IOException {
        files.remove(zipEntry.getName());
      }
    });
    assertEquals(0, files.size());
  }

  public void testIterateGivenEntriesZipInfoCallback() {
    File src = file("demo.zip");
    final Set files = new HashSet();
    files.add("foo.txt");
    files.add("bar.txt");
    files.add("foo1.txt");
    files.add("foo2.txt");

    ZipUtil.iterate(src, new String[] { "foo.txt", "foo1.txt", "foo2.txt" }, new ZipInfoCallback() {

      public void process(ZipEntry zipEntry) throws IOException {
        files.remove(zipEntry.getName());
      }
    });
    assertEquals(1, files.size());
    assertTrue("Wrong entry hasn't been iterated", files.contains("bar.txt"));
  }
  
  public void testIterateGivenEntriesZipEntryCallback() {
    File src = file("demo.zip");
    final Set files = new HashSet();
    files.add("foo.txt");
    files.add("bar.txt");
    files.add("foo1.txt");
    files.add("foo2.txt");

    ZipUtil.iterate(src, new String[] { "foo.txt", "foo1.txt", "foo2.txt" }, new ZipEntryCallback() {
      public void process(InputStream in, ZipEntry zipEntry) throws IOException {
        files.remove(zipEntry.getName());
      }
    });
    assertEquals(1, files.size());
    assertTrue("Wrong entry hasn't been iterated", files.contains("bar.txt"));
  }

  public void testIterateGivenEntriesFromStream() throws IOException {
    File src = file("demo.zip");
    final Set files = new HashSet();
    files.add("foo.txt");
    files.add("bar.txt");
    files.add("foo1.txt");
    files.add("foo2.txt");

    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream(src);
      ZipUtil.iterate(inputStream, new String[] { "foo.txt", "foo1.txt", "foo2.txt" }, new ZipEntryCallback() {
        public void process(InputStream in, ZipEntry zipEntry) throws IOException {
          files.remove(zipEntry.getName());
        }
      });
      assertEquals(1, files.size());
      assertTrue("Wrong entry hasn't been iterated", files.contains("bar.txt"));
    }
    finally {
      if (inputStream != null) {
        inputStream.close();
      }
    }
  }

  public void testIterateAndBreak() {
    File src = file("demo.zip");
    final Set files = new HashSet();
    files.add("foo.txt");
    files.add("bar.txt");
    files.add("foo1.txt");
    files.add("foo2.txt");

    ZipUtil.iterate(src, new ZipEntryCallback() {
      public void process(InputStream in, ZipEntry zipEntry) throws IOException {
        files.remove(zipEntry.getName());
        throw new ZipBreakException();
      }
    });
    assertEquals(3, files.size());
  }

  public void testUnwrapFile() throws Exception {
    File dest = File.createTempFile("temp", null);
    File destDir = File.createTempFile("tempDir", null);
    try {
      destDir.delete();
      destDir.mkdir();
      String child = "TestFile.txt";
      File parent = file(child).getParentFile();
      ZipUtil.pack(parent, dest, true);
      ZipUtil.unwrap(dest, destDir);
      assertTrue((new File(destDir, child)).exists());
    }
    finally {
      FileUtils.forceDelete(destDir);
    }
  }

  public void testUnwrapStream() throws Exception {
    File dest = File.createTempFile("temp", null);
    File destDir = File.createTempFile("tempDir", null);
    InputStream is = null;
    try {
      destDir.delete();
      destDir.mkdir();
      String child = "TestFile.txt";
      File parent = file(child).getParentFile();
      ZipUtil.pack(parent, dest, true);
      is = new FileInputStream(dest);
      ZipUtil.unwrap(is, destDir);
      assertTrue((new File(destDir, child)).exists());
    }
    finally {
      IOUtils.closeQuietly(is);
      FileUtils.forceDelete(destDir);
    }
  }

  public void testUnwrapEntriesInRoot() throws Exception {
    File src = file("demo.zip");
    File destDir = File.createTempFile("tempDir", null);
    try {
      destDir.delete();
      destDir.mkdir();
      ZipUtil.unwrap(src, destDir);
      fail("expected a ZipException, unwrapping with multiple roots is not supported");
    }
    catch (ZipException e) {
      // this is normal outcome
    }
    finally {
      FileUtils.forceDelete(destDir);
    }
  }

  public void testUnwrapMultipleRoots() throws Exception {
    File src = file("demo-dirs-only.zip");
    File destDir = File.createTempFile("tempDir", null);
    try {
      destDir.delete();
      destDir.mkdir();
      ZipUtil.unwrap(src, destDir);
      fail("expected a ZipException, unwrapping with multiple roots is not supported");
    }
    catch (ZipException e) {
      // this is normal outcome
    }
    finally {
      FileUtils.forceDelete(destDir);
    }
  }

  public void testUnwrapSingleRootWithStructure() throws Exception {
    File src = file("demo-single-root-dir.zip");
    File destDir = File.createTempFile("tempDir", null);
    try {
      destDir.delete();
      destDir.mkdir();
      ZipUtil.unwrap(src, destDir);
      assertTrue((new File(destDir, "b.txt")).exists());
      assertTrue((new File(destDir, "bad.txt")).exists());
      assertTrue((new File(destDir, "b")).exists());
      assertTrue((new File(new File(destDir, "b"), "c.txt")).exists());
    }
    finally {
      FileUtils.forceDelete(destDir);
    }
  }

  public void testUnwrapEmptyRootDir() throws Exception {
    File src = file("demo-single-empty-root-dir.zip");
    File destDir = File.createTempFile("tempDir", null);
    try {
      destDir.delete();
      destDir.mkdir();
      ZipUtil.unwrap(src, destDir);
      assertTrue("Dest dir should be empty, root dir was shaved", destDir.list().length == 0);
    }
    finally {
      FileUtils.forceDelete(destDir);
    }
  }

  public void testUnpackEntryDir() throws Exception {
    File src = file("demo-dirs.zip");
    File dest = File.createTempFile("unpackEntryDir", null);
    try {
      ZipUtil.unpackEntry(src, "a", dest);
      assertTrue("Couldn't unpackEntry of a directory entry from a zip!", dest.exists());
      assertTrue("UnpackedEntry of a directory is not a dir!", dest.isDirectory());
    }
    finally {
      FileUtils.forceDelete(dest);
    }

  }


  public void testAddEntryWithCompressionLevelAndDestFile() throws IOException {
      int compressionLevel = ZipEntry.STORED;
      doTestAddEntryWithCompressionLevelAndDestFile(compressionLevel);

      compressionLevel = ZipEntry.DEFLATED;
      doTestAddEntryWithCompressionLevelAndDestFile(compressionLevel);
  }

  private void doTestAddEntryWithCompressionLevelAndDestFile(int compressionLevel) throws IOException {
      File src = file("demo.zip");
      final String fileName = "TestFile.txt";
      if(ZipUtil.containsEntry(src, fileName)) {
        ZipUtil.removeEntry(src, fileName);
      }
      assertFalse(ZipUtil.containsEntry(src, fileName));
      InputStream is = null;
      try {
          is = new FileInputStream(file(fileName));
          byte[] newEntry = IOUtils.toByteArray(is);
          File dest = File.createTempFile("temp.zip", null);
          ZipUtil.addEntry(src, fileName, newEntry, dest, compressionLevel);
          assertTrue(ZipUtil.containsEntry(dest, fileName));

          assertEquals(compressionLevel, ZipUtil.getCompressionLevelOfEntry(dest, fileName));
      } finally {
          IOUtils.closeQuietly(is);
      }
  }

  public void testAddEntryWithCompressionLevelStoredInPlace() throws IOException {
      int compressionLevel = ZipEntry.STORED;
      File src = file("demo.zip");
      File srcCopy = File.createTempFile("ztr", ".zip");
      FileUtils.copyFile(src, srcCopy);
      doTestAddEntryWithCompressionLevelInPlace(srcCopy, compressionLevel);
      FileUtils.forceDelete(srcCopy);
  }

  public void testAddEntryWithCompressionLevelDeflatedInPlace() throws IOException {
      int compressionLevel = ZipEntry.DEFLATED;
      File src = file("demo.zip");
      File srcCopy = File.createTempFile("ztr", ".zip");
      FileUtils.copyFile(src, srcCopy);
      doTestAddEntryWithCompressionLevelInPlace(srcCopy, compressionLevel);
      FileUtils.forceDelete(srcCopy);
  }

  private void doTestAddEntryWithCompressionLevelInPlace(File src, int compressionLevel) throws IOException {
      final String fileName = "TestFile.txt";
      if(ZipUtil.containsEntry(src, fileName)) {
        ZipUtil.removeEntry(src, fileName);
      }
      assertFalse(ZipUtil.containsEntry(src, fileName));
      InputStream is = null;
      try {
          is = new FileInputStream(file(fileName));
          byte[] newEntry = IOUtils.toByteArray(is);
          ZipUtil.addEntry(src, fileName, newEntry, compressionLevel);
          assertTrue(ZipUtil.containsEntry(src, fileName));

          assertEquals(compressionLevel, ZipUtil.getCompressionLevelOfEntry(src, fileName));
      } finally {
          IOUtils.closeQuietly(is);
      }
  }

  public void testReplaceEntryWithCompressionLevel() throws IOException {
    File initialSrc = file("demo.zip");
    File src = File.createTempFile("ztr", ".zip");
    FileUtils.copyFile(initialSrc, src);
    final String fileName = "foo.txt";
    assertTrue(ZipUtil.containsEntry(src, fileName));
    assertEquals(ZipEntry.STORED, ZipUtil.getCompressionLevelOfEntry(src, fileName));
    byte[] content = "testReplaceEntryWithCompressionLevel".getBytes("UTF-8");
    ZipUtil.replaceEntry(src, fileName, content, ZipEntry.DEFLATED);
    assertEquals(ZipEntry.DEFLATED, ZipUtil.getCompressionLevelOfEntry(src, fileName));
    FileUtils.forceDelete(src);
  }

}
