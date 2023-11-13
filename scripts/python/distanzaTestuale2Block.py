from pathlib import Path
import os
import pandas as pd
import re
from Levenshtein import distance
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

# Definisci i due testi


stringa1 = "casa"
stringa2 = "cassa"

# Calcola la distanza di Levenshtein
distanza = distance(stringa1, stringa2)

# Stampa la distanza
print(f"La distanza di Levenshtein tra '{stringa1}' e '{stringa2}' è {distanza}.")


def main():
    folder_path = Path(os.path.abspath(__file__))  # Replace with your folder path
    pathToRoot = str(folder_path.parent.parent.parent)
    print(pathToRoot)
    root_path = Path(pathToRoot)
    print(root_path)
    pathToSpreadsheet = pathToRoot + "\\spreadsheets\\blocking\\"
    path_to_spreadsheet = Path(pathToSpreadsheet)
    print(path_to_spreadsheet)
    files = [file for file in path_to_spreadsheet.iterdir() if file.is_file()]
    print("select file to process:")
    # Print the list of files
    for i in range(len(files)):
        print(str(i) + ") " + str(files[i]))

    fileIndex = int(input())
    fileSelected = files[fileIndex]
    print("You have selected file " + str(fileIndex) + ": " + str(fileSelected))
    spreadsheet = pd.read_excel(io=fileSelected)
    columnName = "Blocco"
    column_elements = spreadsheet[columnName]
    for i in range(0, len(column_elements)):
        try:
            block_name = column_elements[i]
            sheetName = "Blocco "+block_name
            block_sheet = pd.read_excel(io=str(fileSelected), sheet_name=str(sheetName)) #FIXARE
            prima_colonna = block_sheet.iloc[:, 0].tolist()
            promptAmount = len(prima_colonna)
            distanzaAccumulata = 0
            for i in range (0, len(prima_colonna)):
                risultato = splitText(prima_colonna[i], "second: ")
                text1 = risultato[0].lower()
                text2 = risultato[1].lower()
                text1 = removeFromText(text1, "first: ")
                distanza = levenshteinDistance(text1, text2)#distance(text1, text2)
                #print(f"La distanza di Levenshtein tra '{text1}' e '{text2}' è {distanza}.")
                distanzaAccumulata = distanzaAccumulata + distanza
            distanzaMedia = distanzaAccumulata / promptAmount
            print(f"{distanzaMedia}")
        except Exception as e:
            print(e)

def splitText(text, splitter):
    return re.split(splitter, text)
def removeFromText(text, toRemove):
    return text.replace(toRemove, "")

def cosineDistance(text1, text2):
    testo1 = text1
    testo2 = text2
    # Inizializza il vettorizzatore TF-IDF
    tfidf_vectorizer = TfidfVectorizer()
    # Crea la matrice TF-IDF
    matrice_tfidf = tfidf_vectorizer.fit_transform([testo1, testo2])
    # Calcola la similarità coseno
    return (cosine_similarity(matrice_tfidf[0], matrice_tfidf[1]))[0][0]

def levenshteinDistance(text1, text2):
    return distance(text1, text2)

if __name__ == '__main__':
    main()
