package projkurose.core;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileManager {

    public static void saveFile(String path, String filename, String operacao, String filePackate) {
        File file = new File(path + "/" + filename);
        // Escrever no arquivo e salvar.
    }

    public static String checkPath(String path) throws IOException {
        File file = new File(path);
        if (file.isFile()) return "file";
        if (file.isDirectory()) return "diretorio";
        throw new IOException("Arquivo ou diretorio não existe");
    }

    public static File findFile(String path) {
        File file = new File(path);
        if (file.isFile())
            return file;
        return null;
    }

    private static List<String> findFolder(String path_folder) throws IOException {
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

    public static List<String> loadFileList(String path_folder) {
        try (Stream<Path> walk = Files.walk(Paths.get(path_folder))) {
            List<String> result = walk.filter(Files::isRegularFile)
                    .map(x -> x.toString().replaceAll(Matcher.quoteReplacement(File.separator),"/"))
                    .map(x -> x.replaceFirst(path_folder, ""))
                    .collect(Collectors.toList());

            result.forEach(System.out::println);
            return result;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return new ArrayList<>();
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

    public static void sendFileDir(DataInputStream receive, DataOutputStream send, String path_dir, String filename) throws IOException {
//        if (destination.exists())
//            destination.delete();
        File file_sender = null;
        file_sender = new File(path_dir + filename);

        send.writeInt(filename.length());
        send.writeBytes(filename);
        send.writeLong(file_sender.length());

        System.out.println("Enviando arquivo " + filename);

        if (receive.readChar() == 'c') return; // arquivo já esta no cliente (c)ompleto
        try (
                InputStream inputStream = new FileInputStream(file_sender);
        ) {

            byte[] buffer = new byte[4096];
            int bytesRead = -1;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                send.write(buffer, 0, bytesRead);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void receiveFileDir(DataInputStream receive, DataOutputStream send, String path_dir) throws IOException {

        int fileNameLength = receive.readInt();

        String filename = new String(receive.readNBytes(fileNameLength));

        long fileSize = receive.readLong();

        File destination = new File(path_dir + filename);

        String pathFile = destination.getAbsolutePath();

        System.out.println("Recebendo arquivo: " + pathFile);


        pathFile = pathFile.substring(0, pathFile.length() - destination.getName().length());

        File dir = new File(pathFile);

        if (!dir.exists()) dir.mkdirs();

        if (!destination.exists()) destination.createNewFile();
        else if (destination.length() == fileSize) {
            send.writeChar('c');
            /* envia ao servidor que client já possui este arquivo baixado completamente */
            return;
        }
        send.writeChar('n'); /* autoriza sevidor a enviar arquivo */

        try (
                OutputStream outputStream = new FileOutputStream(destination);
        ) {

            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            long count = 0l;
            Long remainingBytes = fileSize;

            while (remainingBytes > 0 && (bytesRead = receive.read(buffer, 0, (int) Math.min(buffer.length, remainingBytes))) > 0) {
                outputStream.write(buffer, 0, bytesRead);
                remainingBytes -= bytesRead;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}

