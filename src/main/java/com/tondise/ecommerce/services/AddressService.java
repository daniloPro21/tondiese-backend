package com.tondise.ecommerce.services;

import com.tondise.ecommerce.dao.dto.AddressDto;
import com.tondise.ecommerce.dao.mappers.AddressMapper;
import com.tondise.ecommerce.dao.models.Address;
import com.tondise.ecommerce.dao.models.User;
import com.tondise.ecommerce.dao.repository.AddressRepository;
import com.tondise.ecommerce.dao.repository.UserRepository;
import com.tondise.ecommerce.dao.request.AddressRequest;
import com.tondise.utils.absrtractServices.AbstractService;
import com.tondise.utils.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AddressService extends AbstractService<Address, AddressDto, AddressRequest> {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;
    private final CacheManager cacheManager;
    public AddressService(AddressRepository addressRepository, UserRepository userRepository, AddressMapper addressMapper, CacheManager cacheManager) {
        super(addressRepository, cacheManager, Address.class.getName());
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.addressMapper = addressMapper;
        this.cacheManager = cacheManager;
    }

    @Transactional(readOnly = true)
    public List<AddressDto> getAddresses(UUID userId) {
        return addressMapper.toDtoList(addressRepository.findByUserId(userId));
    }

    @Transactional(readOnly = true)
    public Page<AddressDto> getAddresses(UUID userId, Pageable pageable) {
        return addressRepository.findByUserId(userId, pageable).map(addressMapper::toDto);
    }

    @Transactional(readOnly = true)
    public long countAddresses(UUID userId) {
        return addressRepository.countByUserId(userId);
    }

    @Transactional(readOnly = true)
    public AddressDto getAddress(UUID userId, UUID addressId) {
        return addressMapper.toDto(findOwnedOrThrow(userId, addressId));
    }

    public AddressDto createAddress(UUID userId, AddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        if (request.isDefault()) {
            unsetPreviousDefault(userId);
        }

        Address address = addressMapper.toModelRequest(request);
        address.setUser(user);

        AddressDto saved = addressMapper.toDto(addressRepository.save(address));
        clearCache();
        return saved;
    }

    public AddressDto updateAddress(UUID userId, UUID addressId, AddressRequest request) {
        Address address = findOwnedOrThrow(userId, addressId);

        if (request.isDefault() && !address.isDefault()) {
            unsetPreviousDefault(userId);
        }

        address.setFirstName(request.getFirstName());
        address.setLastName(request.getLastName());
        address.setAddress(request.getAddress());
        address.setCity(request.getCity());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setPhone(request.getPhone());
        address.setDefault(request.isDefault());

        AddressDto updated = addressMapper.toDto(addressRepository.save(address));
        clearCache();
        return updated;
    }

    public AddressDto softDeleteAddress(UUID userId, UUID addressId) {
        Address address = findOwnedOrThrow(userId, addressId);
        address.setDeleted(true);
        AddressDto deleted = addressMapper.toDto(addressRepository.save(address));
        clearCache();
        return deleted;
    }

    public void deleteAddress(UUID userId, UUID addressId) {
        addressRepository.delete(findOwnedOrThrow(userId, addressId));
        clearCache();
    }

    private Address findOwnedOrThrow(UUID userId, UUID addressId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Adresse introuvable: " + addressId));
    }

    private void unsetPreviousDefault(UUID userId) {
        addressRepository.findByUserId(userId).stream()
                .filter(Address::isDefault)
                .forEach(a -> {
                    a.setDefault(false);
                    addressRepository.save(a);
                });
    }

    @Override
    protected AddressDto convertToDTO(Address model) {
        return addressMapper.toDto(model);
    }

    @Override
    protected Address convertToModel(AddressDto dto) {
        return addressMapper.toModel(dto);
    }

    /**
     * Jamais appelée par {@code AddressController} : {@code POST /addresses} est
     * surchargé pour passer par {@link #createAddress(UUID, AddressRequest)}, seul
     * point d'entrée qui associe l'adresse à l'utilisateur courant. Ce contrat
     * générique ({@code K dto} sans notion d'utilisateur) ne peut pas le faire
     * lui-même — voir {@link com.tondise.ecommerce.controllers.privates.AddressController}.
     */
    @Override
    public AddressDto create(AddressRequest dto) {
        throw new UnsupportedOperationException(
                "Utiliser AddressService.createAddress(userId, request) : une adresse appartient toujours à un utilisateur.");
    }

    /** Voir {@link #create(AddressRequest)} — même raison, utiliser {@link #updateAddress}. */
    @Override
    public AddressDto update(AddressRequest dto, UUID id) {
        throw new UnsupportedOperationException(
                "Utiliser AddressService.updateAddress(userId, id, request) : la propriété doit être vérifiée.");
    }

    @Override
    protected Class<Address> getEntityClass() {
        return Address.class;
    }
}
