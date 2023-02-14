/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package nodos;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NodoS extends javax.swing.JFrame {

    HashMap<Integer, DataOutputStream> douts = new HashMap<>();
    HashMap<Integer, String> tiposDeConeccion = new HashMap<>();
    HashMap<String, Integer> uuids = new HashMap<>();
    HashMap<String, Integer> ids = new HashMap<>();
    int puerto = 0;
    ServerSocket ss = null;
    int counter = 0;
    boolean sum = false, res = false, div = false, mul = false;

    public NodoS(String args[]) {
        initComponents();
        new Thread() {
            @Override
            public void run() {
                System.out.printf("Parametros recibidos: \n");
                for (int i = 0; i < args.length; i++) {
                    //Para cada puerto checo si puedo crear el listener
                    //Si se crea el listener continua
                    //Si no se crea el listener asumimos que otro nodo existe en ese puerto previamente
                    // Creamos un thread y nos conectamos a dicho nodo.
                    System.out.printf("args[%d] = %s \n", i, args[i]);
                    try {
                        try {
                            puerto = Integer.parseInt(args[i]);
                            ss = new ServerSocket(puerto);
                        } catch (BindException e) {
                            System.out.printf("No escuchando \n");
                            try {
                                Socket s = new Socket("localhost", puerto);
                                int c = counter;
                                int p = puerto;
                                System.out.println(" >> Nodo No: " + c + " por iniciar en puerto " + p);

                                hilo(s, c, douts, uuids, ids, tiposDeConeccion, true);

                            } catch (BindException e2) {
                            }
                            counter++;
                            continue;
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(this.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.out.printf("Escuchando \n");
                    break;
                }
                System.out.printf("Termine conexiones iniciales \n\n\n ");
                System.out.printf("Lanzo proceso de revision: \n ");
                CheckConections(tiposDeConeccion, puerto);
                while (true) {
                    try {
                        Socket s = ss.accept();
                        int c = counter;
                        System.out.println(" >> " + "Client No: " + c + " por iniciar!");
                        hilo(s, c, douts, uuids, ids, tiposDeConeccion, false);
                        counter++;
                    } catch (IOException ex) {
                        Logger.getLogger(NodoS.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } //Run

            private void hilo(Socket s, int c, HashMap<Integer, DataOutputStream> douts, HashMap<String, Integer> uuids, HashMap<String, Integer> id, HashMap<Integer, String> tiposDeConeccion, boolean amIClient) {
                // Hilo Genera un Thread que Guarda el output stream de s en el hashtable socks con el numero c
                // Y luego escucha lo que llegue en el input stream de c y lo refleja a todos outputs streams guardados en douts
                // Hilo Termina
                new Thread() {
                    Socket soc;
                    int count;

                    @Override
                    public void run() {
                        soc = s;
                        count = c;
                        String aux, format;
                        try {
                            DataInputStream din = new DataInputStream(soc.getInputStream());
                            DataOutputStream dout = new DataOutputStream(soc.getOutputStream());
                            douts.put(count, dout);
                            String tipoDeConneccion = "";
                            try {
                                if (amIClient) {
                                    //Me identifico ante el serve si es que soy cliente
                                    dout.writeUTF("Nodo");
                                    tiposDeConeccion.put(count, "Nodo");
                                } else {
                                    tipoDeConneccion = din.readUTF();
                                    System.out.println(" >> Un " + tipoDeConneccion);
                                    tiposDeConeccion.put(count, tipoDeConneccion);
                                }
                                while (true) {
                                    String str, uuid, id;
                                    //Se recibe el mensaje por primera vez
                                    str = din.readUTF();
                                    ParseaOperacion mensaje = new ParseaOperacion(str);
                                    uuid = mensaje.uuid;
                                    id = mensaje.id;
                                    if (uuids.containsKey(uuid)) {
                                        int uuidCount = uuids.get(uuid);
                                        uuids.put(uuid, uuidCount + 1);
                                        System.out.printf("Ya lei %s  %d  veces \n", uuid, uuidCount + 1);
                                    } else {
                                        uuids.put(uuid, 0);
                                        for (Map.Entry<Integer, DataOutputStream> set : douts.entrySet()) {
                                            System.out.printf("PROCESO %d ENVIO %s a: %d  \n", count, str, set.getKey());
                                            set.getValue().writeUTF(str);
                                            set.getValue().flush();
                                            format = String.format("PROCESO #%d ENVIO a: %d", count, set.getKey());
                                            if (!historial.getText().equals("")) {
                                                aux = historial.getText();
                                                historial.setText(aux + " \n" + format);
                                            } else {
                                                historial.setText(format);
                                            }
                                        }
                                    }
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(this.getName()).log(Level.SEVERE, null, ex);
                            }
                            din.close();
                            dout.close();
                            System.out.println(" >> " + "Coneccion No: " + count + " fuera de línea! Era " + tipoDeConneccion);
                            douts.remove(c);
                            tiposDeConeccion.remove(c);
                            s.close();
                        } catch (IOException ex) {
                            Logger.getLogger(this.getName()).log(Level.SEVERE, null, ex);
                        } finally {
                            System.out.printf("Client -%d- exit!! ", count);
                        }
                    }
                }.start();
            }

            private void CheckConections(HashMap<Integer, String> tiposDeConeccion, int puerto) {
                new Thread() {
                    @Override
                    public void run() {
                        String aux, format;
                        try {
                            while (true) {
                                int divisiones = 0, sumas = 0, restas = 0, multiplicaciones = 0;
                                System.out.printf("Conexiones | tipo de conexion \n");
                                format = ("Conexiones | tipo de conexion \n");
                                System.out.printf("------------------------------\n");
                                format = (format + "------------------------------\n");
                                for (Map.Entry<Integer, String> set : tiposDeConeccion.entrySet()) {
                                    System.out.printf("| %d           |     %s      \n", set.getKey(), set.getValue());
                                    format = String.format(format + "| %d           |     %s      \n", set.getKey(), set.getValue());
                                    if (set.getValue().compareTo("Resta") == 0) {
                                        restas++;
                                    } else if (set.getValue().compareTo("Suma") == 0) {
                                        sumas++;
                                    } else if (set.getValue().compareTo("Division") == 0) {
                                        divisiones++;
                                    } else if (set.getValue().compareTo("Multiplicacion") == 0) {
                                        multiplicaciones++;
                                    }
                                }
                                if (!conexiones.getText().equals("")) {
                                    aux = conexiones.getText();
                                    conexiones.setText(aux + " \n" + format);
                                } else {
                                    conexiones.setText(format);
                                }
                                System.out.printf("------------------------------\n");
                                try {
                                    if (multiplicaciones < 3 && mul) {
                                        while (multiplicaciones < 3) {
                                            Runtime.getRuntime().exec(String.format("java -jar MultiS/dist/MultiS.jar %d", puerto));
                                            multiplicaciones++;
                                        }
                                    }
                                    if (divisiones < 4 && div) {
                                        while (divisiones < 4) {
                                            Runtime.getRuntime().exec(String.format("java -jar DivS/dist/DivS.jar %d", puerto));
                                            divisiones++;
                                        }
                                    }
                                    if (restas < 2 && res) {
                                        while (restas < 2) {
                                            Runtime.getRuntime().exec(String.format("java -jar RestaS/dist/RestaS.jar %d", puerto));
                                            restas++;
                                        }
                                    }
                                    if (sumas < 2 && sum) {
                                        while (sumas < 2) {
                                            Runtime.getRuntime().exec(String.format("java -jar SumaS/dist/SumaS.jar %d", puerto));
                                            sumas++;
                                        }
                                    }
                                } catch (IOException ex) {
                                    Logger.getLogger(NodoS.class.getName()).log(Level.SEVERE, null, ex);
                                }

                                Thread.sleep(10 * 1000);
                            }
                        } catch (InterruptedException ex) {
                            Logger.getLogger(NodoS.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }.start();
            }
        }.start();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        historial = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        conexiones = new javax.swing.JTextArea();
        clear_hist = new javax.swing.JButton();
        clear_conex = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        B_Suma = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        B_Resta = new javax.swing.JButton();
        B_multiplicacion = new javax.swing.JButton();
        B_division = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        panel = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Historial");

        jLabel2.setText("Conexiones");

        historial.setColumns(20);
        historial.setRows(5);
        jScrollPane1.setViewportView(historial);

        conexiones.setColumns(20);
        conexiones.setRows(5);
        jScrollPane2.setViewportView(conexiones);

        clear_hist.setText("Clear");
        clear_hist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clear_histActionPerformed(evt);
            }
        });

        clear_conex.setText("Clear");
        clear_conex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clear_conexActionPerformed(evt);
            }
        });

        jLabel3.setText("Nodo");

        B_Suma.setText("Suma");
        B_Suma.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                B_SumaActionPerformed(evt);
            }
        });

        jLabel4.setText("Inyección");

        B_Resta.setText("Resta");
        B_Resta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                B_RestaActionPerformed(evt);
            }
        });

        B_multiplicacion.setText("Multiplicacion");
        B_multiplicacion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                B_multiplicacionActionPerformed(evt);
            }
        });

        B_division.setText("Division");
        B_division.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                B_divisionActionPerformed(evt);
            }
        });

        jLabel5.setText("Microservicios");

        panel.setColumns(20);
        panel.setRows(5);
        jScrollPane3.setViewportView(panel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabel1)
                .addGap(31, 31, 31)
                .addComponent(clear_hist)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addGap(54, 54, 54)
                .addComponent(clear_conex)
                .addGap(30, 30, 30))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 284, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(235, 235, 235)
                                .addComponent(jLabel3)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel5))
                                .addGap(24, 24, 24)
                                .addComponent(B_Suma)
                                .addGap(18, 18, 18)
                                .addComponent(B_Resta)
                                .addGap(18, 18, 18)
                                .addComponent(B_multiplicacion)
                                .addGap(18, 18, 18)
                                .addComponent(B_division)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addGap(7, 7, 7)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(clear_conex)
                        .addComponent(clear_hist)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                    .addComponent(jScrollPane1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(B_division, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                    .addComponent(B_Resta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5))
                    .addComponent(B_Suma, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(B_multiplicacion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void clear_histActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clear_histActionPerformed
        historial.setText("");
    }//GEN-LAST:event_clear_histActionPerformed

    private void clear_conexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clear_conexActionPerformed
        conexiones.setText("");
    }//GEN-LAST:event_clear_conexActionPerformed

    private void B_SumaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_B_SumaActionPerformed
        String aux;
        if (!sum) {
            sum = true;
            if (!panel.getText().equals("")) {
                aux = panel.getText();
                panel.setText(aux + "Microservicio Suma inyectado...\n" );
            } else {
                panel.setText("Microservicio Suma inyectado...\n");
            }
        }
        else{
            sum = false;
             if (!panel.getText().equals("")) {
                aux = panel.getText();
                panel.setText(aux + "Microservicio Suma retirado...\n" );
            } else {
                panel.setText("Microservicio Suma retirado...\n");
            }
        }
    }//GEN-LAST:event_B_SumaActionPerformed

    private void B_RestaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_B_RestaActionPerformed
     String aux;
        if (!res) {
            res = true;
            if (!panel.getText().equals("")) {
                aux = panel.getText();
                panel.setText(aux + "Microservicio Resta inyectado...\n" );
            } else {
                panel.setText("Microservicio Resta inyectado...\n");
            }
        }
        else{
            res = false;
             if (!panel.getText().equals("")) {
                aux = panel.getText();
                panel.setText(aux + "Microservicio Resta retirado...\n" );
            } else {
                panel.setText("Microservicio Resta retirado...\n");
            }
        }
    }//GEN-LAST:event_B_RestaActionPerformed

    private void B_multiplicacionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_B_multiplicacionActionPerformed
        String aux;
        if (!mul) {
            mul = true;
            if (!panel.getText().equals("")) {
                aux = panel.getText();
                panel.setText(aux + "Microservicio Multiplicacion inyectado...\n" );
            } else {
                panel.setText("Microservicio Multiplicacion inyectado...\n");
            }
        }
        else{
            mul = false;
             if (!panel.getText().equals("")) {
                aux = panel.getText();
                panel.setText(aux + "Microservicio Multiplicacion retirado...\n" );
            } else {
                panel.setText("Microservicio Multiplicacion retirado...\n");
            }
        }
    }//GEN-LAST:event_B_multiplicacionActionPerformed

    private void B_divisionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_B_divisionActionPerformed
         String aux;
        if (!div) {
            div = true;
            if (!panel.getText().equals("")) {
                aux = panel.getText();
                panel.setText(aux + "Microservicio Division inyectado...\n" );
            } else {
                panel.setText("Microservicio Division  inyectado...\n");
            }
        }
        else{
            div = false;
             if (!panel.getText().equals("")) {
                aux = panel.getText();
                panel.setText(aux + "Microservicio Division retirado...\n" );
            } else {
                panel.setText("Microservicio Division retirado...\n");
            }
        }
    }//GEN-LAST:event_B_divisionActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        System.out.printf("Puertos obtenidos \n\n\n");

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new NodoS(args).setVisible(true);
            }
        });

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton B_Resta;
    private javax.swing.JButton B_Suma;
    private javax.swing.JButton B_division;
    private javax.swing.JButton B_multiplicacion;
    private javax.swing.JButton clear_conex;
    private javax.swing.JButton clear_hist;
    private javax.swing.JTextArea conexiones;
    private javax.swing.JTextArea historial;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextArea panel;
    // End of variables declaration//GEN-END:variables
}

class ParseaOperacion {

    public char operacion;
    public int operador1, operador2;
    public String uuid, id;

    ParseaOperacion(String st) {
        String[] divisiones = st.split(" ");
        id = divisiones[0];
        uuid = divisiones[1];
        operacion = divisiones[3].charAt(0);
        operador1 = Integer.parseInt(divisiones[2]);
        operador2 = Integer.parseInt(divisiones[4]);
    }
}
