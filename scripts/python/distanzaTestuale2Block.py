from pathlib import Path
import os
import pandas as pd

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
    spreadsheet = pd.read_excel(fileSelected)
    columnName = "Blocco"
    column_elements = spreadsheet[columnName]
    for i in range(1, len(column_elements)):
        block_name = column_elements[i]
        print(block_name)
        block_sheet = pd.read_excel(fileSelected, sheet_name=block_name) #FIXARE




if __name__ == '__main__':
    main()
