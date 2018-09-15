package it.uniroma1.lcl.babelarity.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * {@code Enum} che contiene tutti i path che possono servire a {@code MiniBabelNet}.
 */
public enum BabelPath
{
    /**
     * Path delle risorse di {@code MiniBabelNet}.
     */
    RESOURCES_PATH(Paths.get("resources/miniBabelNet/")),
    /**
     * Path al file dizionario di {@code MiniBabelNet} contenente tutti i {@code Synset} con i loro concetti.
     */
    DICTIONARY_FILE_PATH(Paths.get(RESOURCES_PATH.getPath().toString(), "dictionary.txt")),
    /**
     * Path al file contenente i glosse di ogni {@code Synset}.
     */
    GLOSSES_FILE_PATH(Paths.get(RESOURCES_PATH.getPath().toString(), "glosses.txt")),
    /**
     * Path al file contenente i lemmi di ogni {@code Synset}.
     */
    LEMMATIZATION_FILE_PATH(Paths.get(RESOURCES_PATH.getPath().toString(), "lemmatization-en.txt")),
    /**
     * Path al file contenente tutte le relazioni fra {@code Synset}.
     */
    RELATIONS_FILE_PATH(Paths.get(RESOURCES_PATH.getPath().toString(), "relations.txt"));

    private Path path;

    /**
     * Ogni elemento dell'enum è costruito con un Path.
     * @param path {@code Path} legato ad ogni elemento dell'Enum.
     */
    BabelPath(Path path)
    {
        this.path = path;
    }

    /**
     * Metodo che restituisce il {@code Path} legato ad un certo Enum
     * @return L'oggetto {@code Path} legato all'Enum.
     */
    public Path getPath() {
        return path;
    }

    /**
     * Metodo che restituisce il path sottoforma di {@code String}.
     * @return Il path legato all'Enum ma come {@code String}.
     */
    public String getPathString(){return path.toString(); }
}
