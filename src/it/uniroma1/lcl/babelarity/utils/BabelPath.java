package it.uniroma1.lcl.babelarity.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public enum BabelPath
{
    RESOURCES_PATH(Paths.get("resources/miniBabelNet/")),
    DICTIONARY_FILE_PATH(Paths.get(RESOURCES_PATH.getPath().toString(), "dictionary.txt")),
    GLOSSES_FILE_PATH(Paths.get(RESOURCES_PATH.getPath().toString(), "glosses.txt")),
    LEMMATIZATION_FILE_PATH(Paths.get(RESOURCES_PATH.getPath().toString(), "lemmatization-en.txt")),
    RELATIONS_FILE_PATH(Paths.get(RESOURCES_PATH.getPath().toString(), "relations.txt"));

    Path path;
    BabelPath(Path path)
    {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }
    public String getPathString(){return path.toString(); }
}
