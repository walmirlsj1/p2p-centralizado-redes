package app.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileManager {

    public void saveFile(String path, String filename, String operacao, String filePackate) {
        File file = new File(path + "/" + filename);
        // Escrever no arquivo e salvar.
    }

    public static String checkPath(String path) throws IOException {
        File file = new File(path);
        if (file.isFile()) return "file";
        if (file.isDirectory()) return "diretorio";
        throw new IOException("Arquivo ou diretorio não existe");
    }

    public File findFile(String path) {
        File file = new File(path);
        if (file.isFile())
            return file;
        return null;
    }

    private List<String> findFolder(String path_folder) throws IOException {
        File directory = new File(path_folder);
        if (directory.isDirectory()) {
            return loadFileList(path_folder);
        }
        if (!directory.exists()) {
//            directory.mkdirs();
            throw new IOException("Não foi possivel localizar a pasta no diretorio: " + path_folder);
        }

        return null;
    }

    public List<String> loadFileList(String path_folder) {
        try (Stream<Path> walk = Files.walk(Paths.get(path_folder))) {
            List<String> result = walk.filter(Files::isRegularFile).map(x -> x.toString())
                    .collect(Collectors.toList());

            // result.forEach(System.out::println);
            if (!result.isEmpty()) {
//            return true;
                throw new IOException("Erro a pasta Vazia! " + path_folder);
            }
            return result;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static long getSizeFolder(String path) {
        File file = new File(path);

        if (!file.exists()) return 0L;
        if (file.isDirectory()) return getSizeFolder(file);

        return file.length();
    }

    /**
     * https://www.guj.com.br/t/obter-tamanho-de-uma-pasta/40800/2
     * Solução para calcular tamanho da pasta
     */
    private static long getSizeFolder(File dir) {
        long size = 0;
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                size += getSizeFolder(f);
            } else {
                size += f.length();
            }
        }
        return size;
    }

    public void sendFileDir(File source, File destination) throws IOException {
        if (destination.exists())
            destination.delete();

        FileChannel sourceChannel = null;
        FileChannel destinationChannel = null;

        try {
            sourceChannel = new FileInputStream(source).getChannel();
            destinationChannel = new FileOutputStream(destination).getChannel();
            sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
        } finally {
            if (sourceChannel != null && sourceChannel.isOpen())
                sourceChannel.close();
            if (destinationChannel != null && destinationChannel.isOpen())
                destinationChannel.close();
        }
    }
}

