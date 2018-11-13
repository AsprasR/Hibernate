package com.concretepage.util;

import com.concretepage.persistence.Meteorology;
import org.hibernate.HibernateException;
import org.hibernate.PropertyValueException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.PersistenceException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.concretepage.util.HibernateUtil.*;
import static org.junit.Assert.*;

public class HibernateUtilTest {
    private static SessionFactory concreteSessionFactory;
    private static Session session;
    private TreeSet<Meteorology> lists;

    @Before
    public void setUp() throws HibernateException {
        try {
            Properties prop= new Properties();
            prop.setProperty("hibernate.connection.url", "jdbc:postgresql://localhost:5432/testdb?useLegacyDatetimeCode=false&serverTimezone=UTC&createDatabaseIfNotExist=true&UseUnicode=true&characterEncoding=utf8");
            prop.setProperty("hibernate.connection.username", "postgres");
            prop.setProperty("hibernate.connection.password", "root");
            prop.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            prop.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
            prop.setProperty("hibernate.hbm2ddl.auto", "create");
            prop.setProperty("hibernate.connection.CharSet", "utf8");
            prop.setProperty("hibernate.connection.characterEncoding", "utf8" );
            prop.setProperty("hibernate.connection.useUnicode", "true");
            concreteSessionFactory = new Configuration()
                    .addPackage("com.concretepage.persistence")
                    .addProperties(prop)
                    .addAnnotatedClass(Meteorology.class)
                    .buildSessionFactory();
            session = concreteSessionFactory.openSession();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }

        lists = jsonGetRequest("https://danepubliczne.imgw.pl/api/data/synop");
        for( Meteorology o : lists ) {
            addMeteorology(session, o);
        }
    }

    @After
    public void tearDown() {
        session.close();
        concreteSessionFactory.close();
    }

    @Test
    public void AddJsonQueryTest() {
        List<Meteorology> actualResult = listOfMeteorologies(session);

        assertEquals(lists.size(), actualResult.size());
        assertTrue(actualResult.containsAll(lists));
    }

    @Test
    public void AddQueryTest() throws ParseException {
        int id_stacji = 2500;
        String stacja = "Lomza";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
        Date data_pomiaru = sdf.parse("2018-12-12 12" );

        Meteorology meteorology = new Meteorology();
        meteorology.setId_stacji(id_stacji);
        meteorology.setStacja(stacja);
        meteorology.setData_pomiaru(data_pomiaru);

        addMeteorology(session, meteorology);
        lists.add(meteorology);

        List<Meteorology> actualResult = listOfMeteorologies(session);

        assertEquals(lists.size(), actualResult.size());
        assertTrue(actualResult.contains(meteorology));

        for( Meteorology meteo : actualResult ) {
            if( meteo.getId_stacji() == meteorology.getId_stacji() ) {
                assertEquals(id_stacji, meteo.getId_stacji());
                assertEquals(stacja, meteo.getStacja());
                assertEquals(data_pomiaru, meteo.getData_pomiaru());
            }
        }
    }

    @Test
    public void DeleteQueryTest() {
        int deleteId = 12295;
        deleteMeteorology(session, deleteId);
        for (Iterator iterator = lists.iterator(); iterator.hasNext(); ) {
            Meteorology meteorology = (Meteorology) iterator.next();
            if( meteorology.getId_stacji() == deleteId ) {
                iterator.remove();
            }
        }
        List<Meteorology> expectedResult = listOfMeteorologies(session);
        assertEquals(lists.size(), expectedResult.size());
        for( Meteorology meteorology : expectedResult ) {
            assertNotEquals(meteorology.getId_stacji(), 12295);
        }
    }

    @Test
    public void CheckNullFieldTest() throws ParseException {
        Meteorology nullMeteorology = new Meteorology();
        try {
            nullMeteorology.setId_stacji(5000);
            addMeteorology(session, nullMeteorology);
        } catch( PropertyValueException e ) {
            try {
                nullMeteorology.setStacja("Lomza");
                addMeteorology(session, nullMeteorology);
            } catch( PropertyValueException f ) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
                Date data_pomiaru = sdf.parse("2018-03-04 12");
                nullMeteorology.setData_pomiaru(data_pomiaru);
                addMeteorology(session, nullMeteorology);
            }
        }
    }

    @Test(expected=PersistenceException.class)
    public void UpdateToNullTest() {
        int updateId = 12295;
        Meteorology meteo = null;

        for( Meteorology meteorology : lists ) {
            if( meteorology.getId_stacji() ==  updateId ) {
                meteorology.setTemperatura(null);
                meteorology.setPredkosc_wiatru(null);
                meteorology.setKierunek_wiatru(null);
                meteorology.setWilgotnosc_wzgledna(null);
                meteorology.setSuma_opadu(null);
                meteorology.setCisnienie(null);
                meteo = meteorology;
                updateMeteorology(session, meteorology);
            }
        }
        List<Meteorology> actualResult = listOfMeteorologies(session);
        assertTrue(actualResult.contains(meteo));
        for ( Meteorology meteorology : actualResult ) {
            if( meteorology.getId_stacji() == updateId ) {
                assertNull(meteorology.getTemperatura());
                assertNull(meteorology.getPredkosc_wiatru());
                assertNull(meteorology.getKierunek_wiatru());
                assertNull(meteorology.getWilgotnosc_wzgledna());
                assertNull(meteorology.getSuma_opadu());
                assertNull(meteorology.getCisnienie());
            }
        }
        assertNotNull(meteo);
        try {
            meteo.setStacja(null);
            updateMeteorology(session, meteo);
        } catch( PersistenceException e ) {
            meteo.setStacja("Lomza");
            meteo.setData_pomiaru(null);
            updateMeteorology(session, meteo);
        }
    }

    @Test
    public void UpdateQueryTest() throws ParseException {
        int updateId = 12295;
        String stacja = "Łomża";
        String data_pomiaru = "2018-04-04";
        int godzina_pomiaru = 12;
        Float temperatura = (float) 25;
        Integer predkosc_wiatru = 100;
        Integer kierunek_wiatru = 240;
        Float wilgotnosc_wzgledna = (float) 19.9;
        Double suma_opadu = 0.001;
        Float cisnienie = (float) 1220.5;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
        Date date = sdf.parse(data_pomiaru + " " +  godzina_pomiaru);
        Meteorology meteo = null;

        for( Meteorology meteorology : lists ) {
            if( meteorology.getId_stacji() ==  updateId ) {
                meteorology.setStacja(stacja);
                meteorology.setData_pomiaru(date);
                meteorology.setTemperatura(temperatura);
                meteorology.setPredkosc_wiatru(predkosc_wiatru);
                meteorology.setKierunek_wiatru(kierunek_wiatru);
                meteorology.setWilgotnosc_wzgledna(wilgotnosc_wzgledna);
                meteorology.setSuma_opadu(suma_opadu);
                meteorology.setCisnienie(cisnienie);
                meteo = meteorology;
                updateMeteorology(session, meteorology);
            }
        }
        List<Meteorology> actualResult = listOfMeteorologies(session);
        assertTrue(actualResult.contains(meteo));
        for ( Meteorology meteorology : actualResult ) {
            if( meteorology.getId_stacji() == updateId ) {
                assertEquals(stacja, meteorology.getStacja());
                assertEquals(date, meteorology.getData_pomiaru());
                assertEquals(temperatura, meteorology.getTemperatura());
                assertEquals(predkosc_wiatru, meteorology.getPredkosc_wiatru());
                assertEquals(kierunek_wiatru, meteorology.getKierunek_wiatru());
                assertEquals(wilgotnosc_wzgledna, meteorology.getWilgotnosc_wzgledna());
                assertEquals(suma_opadu, meteorology.getSuma_opadu());
                assertEquals(cisnienie, meteorology.getCisnienie());
            }
        }
    }

}