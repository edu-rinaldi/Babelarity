package it.uniroma1.lcl.babelarity.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * {@code Enum} che contiene tutti i path che possono servire a {@code MiniBabelNet}.
 */
public enum BabelPath
{
    RESOURCES_PATH(Paths.get("resources/miniBabelNet/")),
    DICTIONARY_FILE_PATH(Paths.get(RESOURCES_PATH.getPath().toString(), "dictionary.txt")),
    GLOSSES_FILE_PATH(Paths.get(RESOURCES_PATH.getPath().toString(), "glosses.txt")),
    LEMMATIZATION_FILE_PATH(Paths.get(RESOURCES_PATH.getPath().toString(), "lemmatization-en.txt")),
    RELATIONS_FILE_PATH(Paths.get(RESOURCES_PATH.getPath().toString(), "relations.txt"));

    private Path path;

    /**
     *
     * @param path {@code Path} legato ad ogni elemento dell'Enum.
     */
    BabelPath(Path path)
    {
        this.path = path;
    }

    /**
     *
     * @return L'oggetto {@code Path} legato all'Enum.
     */
    public Path getPath() {
        return path;
    }

    /**
     *
     * @return Il path legato all'Enum ma come {@code String}.
     */
    public String getPathString(){return path.toString(); }
}
