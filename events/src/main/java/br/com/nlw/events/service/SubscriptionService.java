package br.com.nlw.events.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.nlw.events.dto.SubscriptionRankingByUser;
import br.com.nlw.events.dto.SubscriptionRankingItem;
import br.com.nlw.events.dto.SubscriptionResponse;
import br.com.nlw.events.exception.EventNotFoundExcption;
import br.com.nlw.events.exception.SubscriptionConflictException;
import br.com.nlw.events.exception.UserIndicatorNotFoundException;
import br.com.nlw.events.model.Event;
import br.com.nlw.events.model.Subscription;
import br.com.nlw.events.model.User;
import br.com.nlw.events.repo.EventRepo;
import br.com.nlw.events.repo.SubscriptionRepo;
import br.com.nlw.events.repo.UserRepo;

@Service
public class SubscriptionService {
	
	@Autowired
	private EventRepo eventRepo;
	
	@Autowired
	private SubscriptionRepo subscriptionRepo;
	
	@Autowired
	private UserRepo userRepo;
	
	public SubscriptionResponse createnewSubscription(String eventName, User user, Integer userId) {

		Event evt = eventRepo.findByPrettyName(eventName);
		if (Objects.isNull(evt)) {
			throw new EventNotFoundExcption(String.format("Evento %s não existe", eventName));
		}
		User userRequest = userRepo.findByEmail(user.getEmail());
		if (Objects.isNull(userRequest)) {
			userRequest = userRepo.save(user);
		}
		User indicator = null;
		if(Objects.nonNull(userId)) {
			indicator = userRepo.findById(userId).orElse(null);
			if (Objects.isNull(indicator)) {
				throw new UserIndicatorNotFoundException("Usuário indicador não existe");
			}
		}
		
		Subscription subs = new Subscription();
		subs.setEvent(evt);
		subs.setSubscriber(userRequest);
		subs.setIndication(indicator);
		
		Subscription tmpSub = subscriptionRepo.findByEventAndSubscriber(evt, userRequest);
		if (Objects.nonNull(tmpSub)) {
			throw new SubscriptionConflictException(String.format("Já existe inscrição para o usuário %s no evento %s", userRequest.getName(), evt.getTitle()));
		}
		
		Subscription res = subscriptionRepo.save(subs);
		return new SubscriptionResponse(res.getSubscriptionNumber(), String.format("https://devstage.com/%s/%s", evt.getPrettyName(), userRequest.getId()));
	}
	
	public List<SubscriptionRankingItem> getCompleteRanking(String prettyName){
		Event evt = eventRepo.findByPrettyName(prettyName);
		if (Objects.isNull(evt)) {
			throw new EventNotFoundExcption(String.format("Ranking do evento %s não existe",prettyName));
		}
		return subscriptionRepo.generateRanking(evt.getEventId());
	}	
	
	public SubscriptionRankingByUser getRankingByUser(String prettyNama, Integer userId) {
		List<SubscriptionRankingItem> ranking = getCompleteRanking(prettyNama);
		
		SubscriptionRankingItem item = ranking.stream().filter(i->i.userId().equals(userId)).findFirst().orElse(null);
		if (Objects.isNull(item)) {
			throw new UserIndicatorNotFoundException("Não há inscrições com indicação do usuario");
		}
		Integer posicao = IntStream.range(0, ranking.size()).filter(pos -> ranking.get(pos).userId().equals(userId)).findFirst().getAsInt();
		return new SubscriptionRankingByUser(item, posicao+1);
	}

}
