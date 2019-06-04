package com.netvox.mail.utilidades;

import com.netvox.mail.ServiciosImpl.CoreMailServicioImpl;
import com.netvox.mail.entidades.Mail;
import com.netvox.mail.servicios.ClienteMongoServicio;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import com.netvox.mail.entidadesfront.Adjunto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service("utilidades")
public class Utilidades {

    @Autowired
    @Qualifier("coremailservicio")
    CoreMailServicioImpl coremailservicio;

    @Autowired
    @Qualifier("clientemongoservicio")
    ClienteMongoServicio clientemongoservicio;

    Logger log = LoggerFactory.getLogger(this.getClass());

    public boolean createFileHTML(Message mensaje, Mail mail, int peso_maximo_adjunto) {
        boolean created = false;
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        int id = mail.getIdcorreo();
        try {
            File unidad = new File(coremailservicio.getRUTA_IN());
            mail.setRuta(unidad.getAbsolutePath());
            String cuerpoMensaje = "";
            String texto = null;
            if (unidad.exists()) {
                unidad.delete();
            }
            unidad.mkdir();
            File adjuntos = new File(unidad.getAbsolutePath() + "/" + id + "/adjuntos");
            adjuntos.mkdirs();
            Object contenidodelmensaje = mensaje.getContent();

            HashMap<String, String> index_nombre_apellido = new HashMap<>();
            if (contenidodelmensaje instanceof String) {  ///esto verifica si el contenido es solamente texto , si lo es se trata como String
                if (mensaje.getContentType().contains("text/html")) {
                    DataHandler dh = mensaje.getDataHandler();
                    OutputStream os = new FileOutputStream(unidad.getAbsolutePath() + "/" + id + ".txt");
                    dh.writeTo(os);
                    os.close();

                    System.out.println("**********PRUEBA STRING**************");
                    System.out.println(contenidodelmensaje.toString());
                    System.out.println("*************************************");

                    if (!index_nombre_apellido.containsKey("[Nombre*] :")) {
                        String linea = mensaje.getContent().toString();
                        if (linea.contains("[Nombre*] :")) {
                            String[] sp = linea.split("\\[Nombre\\*\\] :");
                            linea = sp[1];
                            if (linea.contains("</")) {
                                linea = linea.split("</")[0];
                            }
                            index_nombre_apellido.put("[Nombre*] :", linea);
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
                        }
                    }
                } else {
                    System.out.println("TIPO-2 *********** " + contenidodelmensaje);
                    System.out.println("**********PRUEBA STRING TIPO 2**************");
                    System.out.println(contenidodelmensaje.toString());
                    System.out.println("*************************************");

                    FileWriter fichero = null;
                    PrintWriter pw = null;
                    try {

                        fichero = new FileWriter(unidad.getAbsolutePath() + "/" + id + "/" + id + ".txt");
                        pw = new PrintWriter(fichero);
                        pw.println(contenidodelmensaje);
                    } catch (Exception e) {
                        log.error("error en el createFileHTML", e);
                    } finally {
                        try {
                            if (null != fichero) {
                                fichero.close();
                            }
                        } catch (Exception e2) {
                            log.error("error en el createFileHTML", e2.getCause());
                        }
                    }
                    texto = (String) contenidodelmensaje + " \n";

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
                    cuerpoMensaje = texto;
                }
            } else if (contenidodelmensaje instanceof Multipart) {
                Multipart multipart = (Multipart) contenidodelmensaje;
                int numPart = multipart.getCount();
                ContentType tipocontenido = new ContentType(multipart.getContentType());
                HashMap<String, String> imagenes = new HashMap<>();

                if (tipocontenido.getSubType().equals("ALTERNATIVE")) {
                    for (int i = 0; i < numPart; i++) {
                        Part part = multipart.getBodyPart(i);
                        if (part.isMimeType("text/html")) {
                            cuerpoMensaje = part.getContent().toString();
                            texto = cuerpoMensaje;
                        } else if (part.isMimeType("multipart/*")) {
                            cuerpoMensaje = analizaParteDeMensaje(part, cuerpoMensaje, imagenes, unidad, adjuntos, mail, peso_maximo_adjunto);
                            texto = cuerpoMensaje;
                        }
                    }
                } else {
                    List<Adjunto> listadeadjuntos = new ArrayList<>(); //esta lista es la que incluire en mongo     
                    for (int i = 0; i < numPart; i++) {
                        Part part = multipart.getBodyPart(i);
                        String disposition = part.getDisposition();
                        if (disposition == null) {
                            cuerpoMensaje = analizaParteDeMensaje(part, cuerpoMensaje, imagenes, unidad, adjuntos, mail, peso_maximo_adjunto);
                            String[] array = cuerpoMensaje.split("\\n");
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
                            String rutadeadjunto = adjuntos.getAbsolutePath() + "/" + aux;
                            mbp.saveFile(adjuntos.getAbsolutePath() + "/" + aux);

                            listadeadjuntos.add(new Adjunto(aux, mbp.getSize(), rutadeadjunto));
                            sumarAdjuntos(new File(adjuntos.getAbsolutePath() + "/" + aux), mail, peso_maximo_adjunto);
                        } else if ((disposition != null) && (disposition.equalsIgnoreCase(Part.INLINE))) {
                            analizaParteDeMensaje(part, cuerpoMensaje, imagenes, unidad, adjuntos, mail, peso_maximo_adjunto);
                        }
                    }
                    mongoops.updateFirst(new Query(Criteria.where("idcorreo").is(id)), new Update().set("listadeadjuntos", listadeadjuntos), Mail.class);
                }

                for (String key : imagenes.keySet()) {
                    cuerpoMensaje = cuerpoMensaje.replace(key, imagenes.get(key));
                }

                System.err.println("CUERPO\n");
                System.err.println(cuerpoMensaje);

                //CREACION HTML
                crearArchivoHtml(unidad, cuerpoMensaje, id);
            }
            if (index_nombre_apellido.containsKey("[Nombre*] :")) {
                mail.setNombre(index_nombre_apellido.get("[Nombre*] :"));
            }
            if (index_nombre_apellido.containsKey("[Apellido*] :")) {
                mail.setApellido(index_nombre_apellido.get("[Apellido*] :"));
            }

            mail.setMensaje(cuerpoMensaje);
            mongoops.updateFirst(new Query(Criteria.where("idcorreo").is(id)), new Update().set("mensaje", mail.getMensaje()), Mail.class);
            created = true;
        } catch (IOException | MessagingException ex) {
            log.error("error en el createFileHTML", ex);
        }
        System.out.println("CREADO: " + created);
        return created;
    }
    public static int i = 0;

    public void crearArchivoHtml(File unidad, String cuerpoMensaje, int id) {
        Writer write1 = null;
        try {

            write1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(unidad.getAbsolutePath() + "/" + id + "/" + id + ".txt"), "UTF-8"));
            write1.write(cuerpoMensaje);
        } catch (Exception e) {
            log.error("error en el crearArchivoHtml", e);
        } finally {
            try {
                write1.close();
            } catch (Exception e2) {
                log.error("error en el crearArchivoHtml", e2.getCause());
            }
        }
    }

    public String analizaParteDeMensaje(Part unaParte, String mensaje, HashMap<String, String> imagenes, File unidad, File attach, Mail mail, int peso_maximo_adjunto) {

        try {
            if (unaParte.isMimeType("multipart/alternative")) {
                Multipart mp = (Multipart) unaParte.getContent();
                for (int i = 0; i < mp.getCount(); i++) {
                    Part bp = mp.getBodyPart(i);
                    if (bp.isMimeType("text/plain")) {
                        if (mensaje == null) {
                            mensaje = analizaParteDeMensaje(mp.getBodyPart(i), mensaje, imagenes, unidad, attach, mail, peso_maximo_adjunto);
                        }
                        continue;
                    } else if (bp.isMimeType("text/html")) {
                        mensaje = analizaParteDeMensaje(mp.getBodyPart(i), mensaje, imagenes, unidad, attach, mail, peso_maximo_adjunto);
                        if (mensaje != null) {
                            return mensaje;
                        }
                    } else {
                        return analizaParteDeMensaje(mp.getBodyPart(i), mensaje, imagenes, unidad, attach, mail, peso_maximo_adjunto);
                    }
                }
            } else if (unaParte.isMimeType("multipart/*")) {
                Multipart multi;
                multi = (Multipart) unaParte.getContent();
                System.out.println("*******MULTIPART***************");
                System.out.println(multi.getContentType());
                for (int j = 0; j < multi.getCount(); j++) {
                    mensaje = analizaParteDeMensaje(multi.getBodyPart(j), mensaje, imagenes, unidad, attach, mail, peso_maximo_adjunto);
                }
                System.out.println("********************************");
                System.out.println("                ");
            } else {

                if (unaParte.isMimeType("text/*")) {
                    i += 1;
                    mensaje = mensaje + unaParte.getContent().toString();
                } else {
                    if (unaParte.isMimeType("image/*")) {
                        try {
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
                                fichero = new FileOutputStream(unidad.getAbsolutePath() + "/" + mail.getIdcorreo() + "/embed_" + unaParte.getFileName());
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
                                    imagenes.put("cid:" + contentid, coremailservicio.getPath_entrada() + "/" + mail.getIdcorreo() + "/embed_" + unaParte.getFileName() + "\" id=\"" + imagenes.size());
                                } else {
                                    imagenes.put("cid:" + unaParte.getFileName() + "@", coremailservicio.getPath_entrada() + "/" + mail.getIdcorreo() + "/embed_" + unaParte.getFileName() + "\" id=\"" + imagenes.size());
                                }

                            } catch (Exception ex) {
                                log.error("error en el metodo analizaParteDeMensaje", ex);
                            } finally {
                                fichero.close();
                                imagen.close();
                            }

                        } catch (Exception ex) {
                            log.error("error en el metodo analizaParteDeMensaje", ex);
                        }

                    } else if (unaParte.isMimeType("AUDIO/*")) {
                        String nombrePart = unaParte.getFileName();
                        if (nombrePart == null) {
                            nombrePart = "audio" + i + ".eml";
                        }
                        MimeBodyPart mbp = (MimeBodyPart) unaParte;
                        mbp.saveFile(attach.getAbsolutePath() + "/" + nombrePart);
                        sumarAdjuntos(new File(attach.getAbsolutePath() + "/" + nombrePart), mail, peso_maximo_adjunto);
                    } else if (unaParte.isMimeType("APPLICATION/*")) {
                        String nombrePart = unaParte.getFileName();
                        if (nombrePart == null) {
                            nombrePart = "aplication" + i + ".eml";
                        }
                        MimeBodyPart mbp = (MimeBodyPart) unaParte;
                        mbp.saveFile(attach.getAbsolutePath() + "/" + nombrePart);
                        sumarAdjuntos(new File(attach.getAbsolutePath() + "/" + nombrePart), mail, peso_maximo_adjunto);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mensaje;
    }

    public void sumarAdjuntos(File file, Mail mail, int peso_maximo_adjunto) {
        double limite = 0;
        limite = peso_maximo_adjunto;  //mailajustes.getMaximo_adjunto()
        try {
            int peso_adjunto = Integer.parseInt(String.valueOf(file.length())) / (1024 * 1024);
            mail.setPeso_adjunto(mail.getPeso_adjunto() + peso_adjunto);
            System.out.println("PESO ADJUNTO " + peso_adjunto + "  .PESO PERMITIDO" + limite);
        } catch (NumberFormatException ex) {
            log.error("error en el metodo sumarAdjuntos ", ex);
        }

    }

}
