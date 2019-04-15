package utilidades;

import com.netvox.mail.entidades.Mail;
import com.netvox.mail.entidades.Parametros;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.Normalizer;
import java.util.HashMap;
import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeUtility;

public class Utilidades {

    public static void printException(Exception ex) {
        System.out.println("MSG:" + ex.getMessage() + " ,EXP:" + ex.toString());
        for (int i = 0; i < ex.getStackTrace().length; i++) {
            System.out.println(ex.getStackTrace()[i]);
        }
        ex.printStackTrace();
    }

    public static boolean createFileHTML(Message mensaje, Mail mail, boolean inbound) {
        boolean created = false;
        int id = mail.getId();
        try {

            File unidad = new File((inbound == true ? Parametros.CARPETA_IN : Parametros.CARPETA_OUT) + id);
            mail.setRuta(unidad.getAbsolutePath());
            String cuerpoMensaje = "";
            String texto = null;
            if (unidad.exists()) {
                unidad.delete();
            }
            unidad.mkdir();
            File attach = new File(unidad.getAbsolutePath() + "/ATTACH");
            attach.mkdirs();
            Object o = mensaje.getContent();

            HashMap<String, String> index_nombre_apellido = new HashMap<String, String>();
            if (o instanceof String) {
                if (mensaje.getContentType().indexOf("text/html") != -1) {
                    DataHandler dh = mensaje.getDataHandler();
                    //OutputStream os = new FileOutputStream(unidad.getAbsolutePath() + "/" + id + ".html");
                    OutputStream os = new FileOutputStream(unidad.getAbsolutePath() + "/" + id + ".txt");
                    dh.writeTo(os);
                    os.close();

                    System.out.println("**********PRUEBA STRING**************");
                    System.out.println(o.toString());
                    System.out.println("*************************************");

                    //    System.out.println("TIPO-1 *********** " + mensaje.getContent().toString());
                    if (!index_nombre_apellido.containsKey("[Nombre*] :")) {
                        String linea = mensaje.getContent().toString();
                        if (linea.contains("[Nombre*] :")) {
                            String[] sp = linea.split("\\[Nombre\\*\\] :");
                            linea = sp[1];
                            if (linea.contains("</")) {
                                linea = linea.split("</")[0];
                            }
                            index_nombre_apellido.put("[Nombre*] :", linea);
                            //           System.out.println("CAPTURE NOMBRE : " + linea);
                        }
                    }
                    if (!index_nombre_apellido.containsKey("[Apellido*] :")) {
                        String linea = mensaje.getContent().toString();
                        if (linea.contains("[Apellido*] :")) {
                            String[] sp = linea.split("\\[Apellido\\*\\] :");
                            linea = sp[1];
                            if (linea.contains("</")) {
                                linea = linea.split("</")[0];
                            }
                            index_nombre_apellido.put("[Apellido*] :", linea);
                            //        System.out.println("CAPTURE APELLIDO : " + linea);
                        }
                    }
                } else {
                    System.out.println("TIPO-2 *********** " + o);

                    System.out.println("**********PRUEBA STRING TIPO 2**************");
                    System.out.println(o.toString());
                    System.out.println("*************************************");

                    FileWriter fichero = null;
                    PrintWriter pw = null;
                    try {
                        //fichero = new FileWriter(unidad.getAbsolutePath() + "/" + id + ".html");
                        fichero = new FileWriter(unidad.getAbsolutePath() + "/" + id + ".txt");
                        pw = new PrintWriter(fichero);
                        pw.println(o);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (null != fichero) {
                                fichero.close();
                            }
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                    texto = (String) o + " \n";

                    if (!index_nombre_apellido.containsKey("[Nombre*] :")) {
                        String linea = texto;
                        if (linea.contains("[Nombre*] :")) {
                            linea = linea.replace("[Nombre*] :", "");
                            index_nombre_apellido.put("[Nombre*] :", linea);
                            System.out.println("CAPTURE NOMBRE : " + linea);
                        }
                    }
                    if (!index_nombre_apellido.containsKey("[Apellido*] :")) {
                        String linea = texto;
                        if (linea.contains("[Apellido*] :")) {
                            linea = linea.replace("[Apellido*] :", "");
                            index_nombre_apellido.put("[Apellido*] :", linea);
                            System.out.println("CAPTURE APELLIDO : " + linea);
                        }
                    }
                }
            } else if (o instanceof Multipart) {
                Multipart mp = (Multipart) o;
                int numPart = mp.getCount();
                ContentType c = new ContentType(mp.getContentType());
                HashMap<String, String> imagenes = new HashMap<String, String>();

                if (c.getSubType().equals("ALTERNATIVE")) {
                    for (int i = 0; i < numPart; i++) {
                        Part part = mp.getBodyPart(i);
                        if (part.isMimeType("text/html")) {
                            cuerpoMensaje = part.getContent().toString();
                            texto = cuerpoMensaje;
                        } else if (part.isMimeType("multipart/*")) {
                            cuerpoMensaje = analizaParteDeMensaje(part, cuerpoMensaje, imagenes, unidad, attach, mail);
                            texto = cuerpoMensaje;
                        }
                    }
                } else {
                    for (int i = 0; i < numPart; i++) {
                        Part part = mp.getBodyPart(i);
                        String disposition = part.getDisposition();
                        if (disposition == null) {

                            cuerpoMensaje = analizaParteDeMensaje(part, cuerpoMensaje, imagenes, unidad, attach, mail);
                            //System.out.println("*****\n"+cuerpoMensaje+"\n*****");

                            String[] array = cuerpoMensaje.split("\\n");
                            // System.out.println("numero de partes " + array.length);
                            bucle:
                            for (int x = 0; x < array.length; x++) {
                                String linea = array[x];
                                if (!index_nombre_apellido.containsKey("[Nombre*] :")) {
                                    if (linea.contains("[Nombre*] :")) {
                                        linea = linea.split("\\[Nombre\\*\\] :")[1];
                                        index_nombre_apellido.put("[Nombre*] :", linea);
                                        System.out.println("CAPTURE NOMBRE : " + linea);
                                    }
                                } else if (!index_nombre_apellido.containsKey("[Apellido*] :")) {
                                    if (linea.contains("[Apellido*] :")) {
                                        linea = linea.split("\\[Apellido\\*\\] :")[1];
                                        index_nombre_apellido.put("[Apellido*] :", linea);
                                        System.out.println("CAPTURE APELLIDO : " + linea);
                                    }
                                } else {
                                    break bucle;
                                }
                            }

                            if (part.isMimeType("image/*")) {
                                continue;
                            }

                            if (mail.getRemitente() == null) {
                                mail.setRemitente("");
                            }

                            System.out.println("*************************");
                            System.out.println("CUERPO MENSAJE");
                            System.out.println(cuerpoMensaje);
                            System.out.println("*************************");

                            boolean gmail = false;
                            if (mail.getRemitente() != null) {
                                if (mail.getRemitente().endsWith("@gmail.com") && cuerpoMensaje.split("<div").length > 1) {
                                    gmail = true;
                                    texto = cuerpoMensaje;
                                } else if (cuerpoMensaje.split("<html").length > 1) {
                                    int largo = cuerpoMensaje.split("<html").length;
                                    texto = cuerpoMensaje.split("<html")[0];
                                    cuerpoMensaje = "<html " + cuerpoMensaje.split("<html")[largo - 1];
                                } else {
                                    texto = cuerpoMensaje;
                                }
                            }

                            //  System.out.println("GMAIL : " + gmail + " ,MAIL:" + cuerpoMensaje);
                        } else if ((disposition != null) && (disposition.equalsIgnoreCase(Part.ATTACHMENT))) {
                            String nombrePart = part.getFileName();
                            String aux = "";
                            if (nombrePart == null) {
                                aux = "adjunto" + i + ".eml";
                            } else {
                                aux = MimeUtility.decodeText(part.getFileName());
                                aux = Normalizer.normalize(aux, Normalizer.Form.NFC);
                            }
                            System.out.println("NOMBRE ADJUNTO " + aux);
                            MimeBodyPart mbp = (MimeBodyPart) part;
                            mbp.saveFile(attach.getAbsolutePath() + "/" + aux);
                            sumarAdjuntos(new File(attach.getAbsolutePath() + "/" + aux), mail);
                        } else if ((disposition != null) && (disposition.equalsIgnoreCase(Part.INLINE))) {
                            analizaParteDeMensaje(part, cuerpoMensaje, imagenes, unidad, attach, mail);
                        }
                    }

                }

                System.err.println("CUERPO111\n");
                System.err.println(cuerpoMensaje);

                for (String key : imagenes.keySet()) {
                    cuerpoMensaje = cuerpoMensaje.replace(key, imagenes.get(key));
                }

                System.err.println("CUERPO222\n");
                System.err.println(cuerpoMensaje);

                //CREACION HTML
                Writer write1 = null;
                try {
                    //iso-8859-1
                    //write1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(unidad.getAbsolutePath() + "/" + id + ".html"), "UTF-8"));
                    write1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(unidad.getAbsolutePath() + "/" + id + ".txt"), "UTF-8"));
                    write1.write(cuerpoMensaje);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        write1.close();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }

                //CREACION TEXT
                /*    Writer write2 = null;
                try {
                    //write2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(unidad.getAbsolutePath() + "/" + id + ".txt"), "iso-8859-1"));
                    write2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(unidad.getAbsolutePath() + "/" + id + ".txt"), "UTF-8"));
                    write2.write(texto);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    write2.close();
                } */
            }

            if (index_nombre_apellido.containsKey("[Nombre*] :")) {
                mail.setNombre(index_nombre_apellido.get("[Nombre*] :"));
            }

            if (index_nombre_apellido.containsKey("[Apellido*] :")) {
                mail.setApellido(index_nombre_apellido.get("[Apellido*] :"));
            }

            mail.setTexto(texto);
            created = true;
        } catch (Exception ex) {
            Utilidades.printException(ex);
        }
        System.out.println("CREADO=> " + created);
        return created;
    }
    public static int i = 0;

    static String analizaParteDeMensaje(Part unaParte, String mensaje, HashMap<String, String> imagenes, File unidad, File attach, Mail mail) {

        try {

            if (unaParte.isMimeType("multipart/alternative")) {
                Multipart mp = (Multipart) unaParte.getContent();
                for (int i = 0; i < mp.getCount(); i++) {
                    Part bp = mp.getBodyPart(i);
                    if (bp.isMimeType("text/plain")) {
                        if (mensaje == null) {
                            mensaje = analizaParteDeMensaje(mp.getBodyPart(i), mensaje, imagenes, unidad, attach, mail);
                        }
                        continue;
                    } else if (bp.isMimeType("text/html")) {
                        mensaje = analizaParteDeMensaje(mp.getBodyPart(i), mensaje, imagenes, unidad, attach, mail);
                        if (mensaje != null) {
                            return mensaje;
                        }
                    } else {
                        return analizaParteDeMensaje(mp.getBodyPart(i), mensaje, imagenes, unidad, attach, mail);
                    }
                }
            } else if (unaParte.isMimeType("multipart/*")) {
                Multipart multi;
                multi = (Multipart) unaParte.getContent();
                System.out.println("*******MULTIPART***************");
                System.out.println(multi.getContentType());
                for (int j = 0; j < multi.getCount(); j++) {
                    mensaje = analizaParteDeMensaje(multi.getBodyPart(j), mensaje, imagenes, unidad, attach, mail);
                }
                System.out.println("********************************");
                System.out.println("                ");
            } else {

                if (unaParte.isMimeType("text/*")) {
                    i += 1;
                    //   System.out.println(i + " : " + unaParte.getContent().toString());
                    mensaje = mensaje + unaParte.getContent().toString();
                } else {
                    if (unaParte.isMimeType("image/*")) {
                        //  System.out.println("DETECT  " + unaParte.getDescription() + " -- " + unaParte.getDataHandler() + " __ " + unaParte.getContentType());
                        try {
                            System.out.println(unidad.getAbsolutePath() + "/" + unaParte.getFileName());
                            boolean depurar = false;
                            if (unaParte.getFileName() == null) {
                                return mensaje;
                            }

                            if (unaParte.getFileName().contains("?")) {
                                depurar = true;
                                unaParte.setFileName(unaParte.getFileName().replace("?", ""));
                            }
                            if (unaParte.getFileName().contains("=")) {
                                depurar = true;
                                unaParte.setFileName(unaParte.getFileName().replace("=", ""));
                            }
                            if (depurar) {
                                System.out.println("MODIFIED : " + unidad.getAbsolutePath() + "/" + unaParte.getFileName());
                            }
                            FileOutputStream fichero = null;
                            InputStream imagen = null;
                            try {
                                fichero = new FileOutputStream(unidad.getAbsolutePath() + "/embed_" + unaParte.getFileName());
                                imagen = unaParte.getInputStream();
                                byte[] bytes = new byte[1000];
                                int leidos = 0;
                                while ((leidos = imagen.read(bytes)) > 0) {
                                    fichero.write(bytes, 0, leidos);
                                }
                                MimeBodyPart mbp = (MimeBodyPart) unaParte;
                                String contentid = mbp.getContentID();
                                if (contentid != null) {
                                    contentid = contentid.replace("<", "");
                                    contentid = contentid.replace(">", "");
                                    imagenes.put("cid:" + contentid, Parametros.URL_IN + mail.getId() + "/embed_" + unaParte.getFileName() + "\" id=\"" + imagenes.size());
                                } else {
                                    imagenes.put("cid:" + unaParte.getFileName() + "@", Parametros.URL_IN + mail.getId() + "/embed_" + unaParte.getFileName() + "\" id=\"" + imagenes.size());
                                }

                            } catch (Exception ex) {
                                Utilidades.printException(ex);
                            } finally {
                                fichero.close();
                                imagen.close();
                            }

                        } catch (Exception ex) {
                            Utilidades.printException(ex);
                        }

                    } else if (unaParte.isMimeType("AUDIO/*")) {
                        String nombrePart = unaParte.getFileName();
                        if (nombrePart == null) {
                            nombrePart = "audio" + i + ".eml";
                        }
                        MimeBodyPart mbp = (MimeBodyPart) unaParte;
                        mbp.saveFile(attach.getAbsolutePath() + "/" + nombrePart);
                        sumarAdjuntos(new File(attach.getAbsolutePath() + "/" + nombrePart), mail);
                    } else if (unaParte.isMimeType("APPLICATION/*")) {
                        String nombrePart = unaParte.getFileName();
                        if (nombrePart == null) {
                            nombrePart = "aplication" + i + ".eml";
                        }
                        MimeBodyPart mbp = (MimeBodyPart) unaParte;
                        mbp.saveFile(attach.getAbsolutePath() + "/" + nombrePart);
                        sumarAdjuntos(new File(attach.getAbsolutePath() + "/" + nombrePart), mail);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mensaje;
    }

    public static void sumarAdjuntos(File file, Mail mail) {
        double limite = 0;
        limite = Parametros.MAXIMO_PESO_ADJUNTO_INBOUND;
        try {
            Double peso_adjunto = Double.parseDouble(String.valueOf(file.length())) / (1024 * 1024);
            mail.setPeso_adjunto(mail.getPeso_adjunto() + peso_adjunto);
            System.out.println("PESO ADJUNTO " + peso_adjunto + "  .PESO PERMITIDO" + limite);
        } catch (Exception ex) {
            Utilidades.printException(ex);
        }

    }

}
