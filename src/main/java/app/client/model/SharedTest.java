package app.client.model;

import app.base.FileManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SharedTest {
    public static void main(String[] args) {
        BufferedReader in =
                new BufferedReader(new InputStreamReader(System.in));
        SharedDAO dao = new SharedDAO();
        String continuar = "Y";


        while (continuar.equals("Y")) {
            try {
                System.out.println("Informe titulo do compartilhamento: ");
                String title = in.readLine();
                System.out.println("Informe diretorio do compartilhamento: ");
                String diretorio = in.readLine();

                System.out.println("Digite Y para continuar:");
                continuar = in.readLine();

                Long size = FileManager.getSizeFolder(diretorio);

                Shared shared = new Shared(null, title, diretorio, size);

                dao.insert(shared);

            } catch (IOException e) {
                System.out.println("Error I/O");
            }
        }
    }
}
