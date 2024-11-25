package hello.integration.service;

import hello.integration.repository.MusicStateDTO;
import org.springframework.stereotype.Service;

@Service
public class MusicStateService {
    private MusicStateDTO currentState;

    public synchronized void updateState(MusicStateDTO state) {
        this.currentState = state;
    }

    public synchronized MusicStateDTO getCurrentState() {
        return currentState;
    }
}
