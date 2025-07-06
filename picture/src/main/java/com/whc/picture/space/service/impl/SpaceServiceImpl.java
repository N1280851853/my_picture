package com.whc.picture.space.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whc.picture.entity.space.common.SpaceLevelEnum;
import com.whc.picture.entity.space.entity.SpaceDO;
import com.whc.picture.entity.user.UserDO;
import com.whc.picture.exception.BusinessException;
import com.whc.picture.exception.ErrorCode;
import com.whc.picture.space.controller.qo.SaveSpaceQO;
import com.whc.picture.space.controller.qo.SpaceIdQO;
import com.whc.picture.space.controller.qo.UpdateSpaceQO;
import com.whc.picture.space.service.SpaceService;
import com.whc.picture.space.mapper.SpaceMapper;
import com.whc.picture.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 23161
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2025-06-30 20:38:04
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, SpaceDO>
        implements SpaceService {

    @Resource
    private UserService userService;

    @Resource
    private TransactionTemplate transactionTemplate;

    Map<Long, Object> lockMap = new ConcurrentHashMap<>();

    @Override
    public Long saveSpace(SaveSpaceQO qo, HttpServletRequest request) {
        SpaceDO spaceDO = new SpaceDO();
        String spaceName = qo.getSpaceName();
        Integer spaceLevel = qo.getSpaceLevel();
        if (StrUtil.isEmpty(spaceName)) {
            spaceName = "默认空间";
        }
        if (null == spaceLevel) {
            spaceLevel = 0;
        }
        spaceDO.setSpaceName(spaceName)
                .setSpaceLevel(spaceLevel);
        // 填充空间大小和容量
        fillSpaceBySpaceLevel(spaceDO);

        // 校验权限
        UserDO loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        spaceDO.setUserId(userId);

        if (!userService.isAdmin(loginUser)) {
            // 非管理员只能创建普通级别的空间
            spaceDO.setSpaceLevel(0);
        }

        // 控制同一个用户只能创建一个私有空间
        Object lock = lockMap.computeIfAbsent(userId, key -> new Object());

        synchronized (lock) {
            try {
                // 判断是否已有空间
                return transactionTemplate.execute(status -> {
                    // 判断是否已有空间
                    boolean exists = this.lambdaQuery()
                            .select(SpaceDO::getId)
                            .eq(SpaceDO::getUserId, userId)
                            .exists();
                    if (exists) {
                        throw new BusinessException(ErrorCode.OPERATION_ERROR, "每个用户仅能有一个私有空间");
                    }
                    this.save(spaceDO);

                    return spaceDO.getId();
                });
            } finally {
                // 防止内存泄漏
                lockMap.remove(userId);
            }
        }

         /*
             这里如果使用 @Transactional 声明式事务，则会产生一个问题，就的当 A 事务拿到锁，进入以下代码块中判断时没有创建过空间，
             然后创建了一个空间，但是此时由于使用声明式事务，数据需要等方法执行完成后才会真正的插入到数据库中，
             显然此时方法并没执行完成，然后A事务释放锁，B事务获取到锁，B事务进行判断，由于A事务还没有提交，
             所以没有创建过空间，同样执行保存空间方法，就造成一个用户创建两个空间
            解决方法： 编程式事务
        */
    }

    @Override
    public void updateSpace(UpdateSpaceQO qo, HttpServletRequest request) {
        Long spaceId = qo.getId();
        SpaceDO spaceDO = this.lambdaQuery()
                .eq(SpaceDO::getId, spaceId)
                .one();
        spaceDO.setSpaceName(qo.getSpaceName())
                .setSpaceLevel(qo.getSpaceLevel())
                .setMaxSize(qo.getMaxSize())
                .setMaxCount(qo.getMaxCount());
        // 填充参数
        fillSpaceBySpaceLevel(spaceDO);
        this.updateById(spaceDO);
    }

    @Override
    public void deleteSpaceById(SpaceIdQO qo, HttpServletRequest request) {
        Long spaceId = qo.getId();
        UserDO loginUser = userService.getLoginUser(request);

        SpaceDO spaceDO = this.lambdaQuery()
                .select(SpaceDO::getId, SpaceDO::getUserId)
                .eq(SpaceDO::getId, spaceId)
                .one();
        if (!spaceDO.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        this.removeById(spaceId);
    }

    @Override
    public void fillSpaceBySpaceLevel(SpaceDO space) {
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum != null) {
            long maxSize = spaceLevelEnum.getMaxSize();
            // 当管理员没有指定最大图片数量时才设置为默认的最大图片数量
            if (null == space.getMaxSize()) {
                space.setMaxSize(maxSize);
            }
            long maxCount = spaceLevelEnum.getMaxCount();
            // 当管理员没有指定最大容量时才设置为默认的最大容量
            if (null == space.getMaxCount()) {
                space.setMaxCount(maxCount);
            }
        }
    }
}




