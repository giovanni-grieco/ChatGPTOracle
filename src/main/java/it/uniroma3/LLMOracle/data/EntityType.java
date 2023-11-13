package it.uniroma3.LLMOracle.data;

public enum EntityType {
    CAMERA(){
        @Override
        public int getTypeIndex(){
            return 0;
        }
    },
    MONITOR(){
        @Override
        public int getTypeIndex() {
            return 1;
        }
    },
    NOTEBOOK(){
        @Override
        public int getTypeIndex() {
            return 2;
        }
    };
    public abstract int getTypeIndex();
}
